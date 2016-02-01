package me.akuz.mnist.digits.visor.algo;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.StatsUtils;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;

/**
 * Multiple Normal Inverse-Gamma processes along
 * the last dimension of the specified tensor shape.
 *
 */
public final class MNIGClasses {
	
	public static final int STAT_SUM = 0;
	public static final int STAT_SUM_SQ = 1;
	public static final int STATS_COUNT = 2;
	
	private final int _outputClassCount;
	private final int _inputChannelCount;
	
	private final Shape  _classShape;
	private final Tensor _classCountPrior;
	private final Tensor _classCountAdded;
	
	private final Shape  _classStatsShape;
	private final Tensor _classStatsPrior;
	private final Tensor _classStatsAdded;
	
	public MNIGClasses(
			final int outputClassCount,
			final int inputChannelCount) {
		
		if (outputClassCount < 2) {
			throw new IllegalArgumentException(
					"outputClassCount must be >= 2, got " + 
					outputClassCount);
		}
		_outputClassCount = outputClassCount;
		
		if (inputChannelCount < 1) {
			throw new IllegalArgumentException(
					"inputChannelCount must be >= 1, got " + 
					inputChannelCount);
		}
		_inputChannelCount = inputChannelCount;
		
		_classShape = new Shape(_outputClassCount);
		_classStatsShape = new Shape(_outputClassCount, _inputChannelCount, STATS_COUNT);

		_classCountPrior = new Tensor(_classShape);
		_classCountAdded = new Tensor(_classShape);
		
		_classStatsPrior = new Tensor(_classStatsShape);
		_classStatsAdded = new Tensor(_classStatsShape);
		
		// set class priors
		for (int classIdx=0; classIdx<_outputClassCount; classIdx++) {
			// TODO from arguments
			_classCountPrior.set(classIdx, 1.0);
		}
		
		// set class stats priors
		final Random rnd = ThreadLocalRandom.current();
		final int[] statsIndices = new int[_classStatsShape.ndim];
		final Location statsLoc = new Location(statsIndices);
		for (int classIdx=0; classIdx<_outputClassCount; classIdx++) {
			statsIndices[0] = classIdx;
			for (int channelIdx=0; channelIdx<_inputChannelCount; channelIdx++) {
				statsIndices[1] = channelIdx;
				final int statsIdx = _classStatsShape.calcFlatIndexFromLocation(statsLoc);
				
				// TODO from arguments
				_classStatsPrior.set(statsIdx + STAT_SUM, 0.5 + rnd.nextDouble()*0.1);
				_classStatsPrior.set(statsIdx + STAT_SUM_SQ, 1.0);
			}
		}
	}
	
	public void clearObservations() {
		_classCountAdded.fill(0.0);
		_classStatsAdded.fill(0.0);
	}
	
	public void addObservation(
			final double[] classProbs,
			final int classProbsStart,
			final double[] channelData,
			final int channelDataStart) {
		
		if (classProbs == null) {
			throw new NullPointerException("classWeights");
		}
		if (channelData == null) {
			throw new NullPointerException("channelData");
		}
		
		// write location
		final int[] statsIndices = new int[_classStatsShape.ndim];
		final Location statsLocation = new Location(statsIndices);
		
		for (int classIdx = 0; classIdx < _outputClassCount; classIdx++) {
			
			statsIndices[0] = classIdx;
			final double classProb = classProbs[classProbsStart + classIdx];
			
			// add to class counts
			_classCountAdded.add(classIdx, classProb);
			
			for (int channelIdx = 0; channelIdx < _inputChannelCount; channelIdx++) {
				
				statsIndices[1] = channelIdx;
				final double channelValue = channelData[channelDataStart + channelIdx];
				
				// calculate stats index
				final int statsStartIdx = _classStatsShape.calcFlatIndexFromLocation(statsLocation);
	
				// add to class stats
				_classStatsAdded.add(statsStartIdx + STAT_SUM, classProb*channelValue);
				_classStatsAdded.add(statsStartIdx + STAT_SUM_SQ, classProb*channelValue*channelValue);
			}
		}
	}
	
	final void fillClassProbs(
			final double[] classProbs,
			final int classProbsStart,
			final double[] channelData,
			final int channelDataStart) {
		
		// write location
		final int[] statsIndices = new int[_classStatsShape.ndim];
		final Location statsLocation = new Location(statsIndices);
		
		// calculate posterior log likelihood
		for (int classIdx = 0; classIdx < _outputClassCount; classIdx++) {
			
			statsIndices[0] = classIdx;
			
			final double priorClassCount = _classCountPrior.get(classIdx);
			final double addedClassCount = _classCountAdded.get(classIdx);
			
			final double posteriorClassCount = 
					priorClassCount +
					addedClassCount;
			
			// init with class likelihood
			classProbs[classIdx] = Math.log(posteriorClassCount);

			final double priorSamples = _classCountPrior.get(classIdx);
			final double addedSamples = _classCountAdded.get(classIdx);
			
			// add class stats likelihoods
			for (int channelIdx = 0; channelIdx < _inputChannelCount; channelIdx++) {
				
				statsIndices[1] = channelIdx;
				
				// calculate stats index
				final int statsStartIdx = _classStatsShape.calcFlatIndexFromLocation(statsLocation);
				
				// get accumulated stats
				final double priorSum = _classStatsPrior.get(statsStartIdx + STAT_SUM);
				final double priorSumSq = _classStatsPrior.get(statsStartIdx + STAT_SUM);
				
				// get accumulated stats
				final double addedSum = _classStatsAdded.get(statsStartIdx + STAT_SUM);
				final double addedSumSq = _classStatsAdded.get(statsStartIdx + STAT_SUM);
				
				// TODO
				
			}
		}
		
		// normalize to probabilities
		StatsUtils.logLikesToProbsInPlace(
				classProbs, 
				classProbsStart,
				_outputClassCount);
	}
	
	final void fillPosteriorMean(
			final double[] classProbs,
			final int classProbsStart,
			final double[] channelData,
			final int channelDataStart) {

		if (classProbs == null) {
			throw new NullPointerException("classWeights");
		}
		if (channelData == null) {
			throw new NullPointerException("channelData");
		}
		
		// write location
		final int[] statsIndices = new int[_classStatsShape.ndim];
		final Location statsLocation = new Location(statsIndices);
		
		// first reset means to zero
		for (int channelIdx = 0; channelIdx < _inputChannelCount; channelIdx++) {
			channelData[channelDataStart + channelIdx] = 0.0;
		}
		
		// populate means using class probabilities
		for (int classIdx = 0; classIdx < _outputClassCount; classIdx++) {
			
			statsIndices[0] = classIdx;
			final double classProb = classProbs[classProbsStart + classIdx];
			
			final double priorSamples = _classCountPrior.get(classIdx);
			final double addedSamples = _classCountAdded.get(classIdx);

			for (int channelIdx = 0; channelIdx < _inputChannelCount; channelIdx++) {
				
				statsIndices[1] = channelIdx;
				
				// calculate stats index
				final int statsStartIdx = _classStatsShape.calcFlatIndexFromLocation(statsLocation);
				
				// get sum statistics
				final double priorSum = _classStatsPrior.get(statsStartIdx + STAT_SUM);
				final double addedSum = _classStatsAdded.get(statsStartIdx + STAT_SUM);
				
				// calculate posterior mean
				final double posteriorMean = 
						(priorSum + addedSum) /
						(priorSamples + addedSamples);
				
				channelData[channelDataStart + channelIdx] 
						+= classProb*posteriorMean;
			}
		}
	}
	

}
