package me.akuz.mnist.digits.visor.algo;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.StatsUtils;
import me.akuz.core.math.StudentT;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;

/**
 * Gaussian multi-channel classes.
 *
 */
public final class GaussClasses {
	
	private static final double LOG_INSURANCE = 1e-9;
	
	public enum PriorUse {
		INNER,
		OUTER
	}
	
	public static final int PRIOR_STAT_MEAN = 0;
	public static final int PRIOR_STAT_NYU = 1;
	public static final int PRIOR_STAT_ALPHA = 2;
	public static final int PRIOR_STAT_BETA = 3;
	public static final int LENGTH_PRIOR_STAT = 4;
	
	public static final int ADDED_STAT_SUM = 0;
	public static final int ADDED_STAT_SUM_SQ = 1;
	public static final int ADDED_STAT_WEIGHT = 2;
	public static final int LENGTH_ADDED_STAT = 3;

	private final int _classCount;
	private final int _channelCount;
	
	private final Tensor _classPrior;
	private final Tensor _classAdded;
	
	private final Tensor _classPriorStats;
	private final Tensor _classAddedStats;
	
	public GaussClasses(
			final int classCount,
			final int channelCount) {
		
		if (classCount < 2) {
			throw new IllegalArgumentException(
					"classCount must be >= 2, got " + 
					classCount);
		}
		_classCount = classCount;
		
		if (channelCount < 1) {
			throw new IllegalArgumentException(
					"channelCount must be >= 1, got " + 
					channelCount);
		}
		_channelCount = channelCount;
		
		_classPrior = new Tensor(new Shape(_classCount));
		_classAdded = new Tensor(new Shape(_classCount));
		
		_classPriorStats = new Tensor(new Shape(_classCount, _channelCount, LENGTH_PRIOR_STAT));
		_classAddedStats = new Tensor(new Shape(_classCount, _channelCount, LENGTH_ADDED_STAT));
		
		// set class and class stats priors
		final Random rnd = ThreadLocalRandom.current();
		final int[] priorStatsIndices = new int[_classPriorStats.ndim];
		final Location priorStatsLoc = new Location(priorStatsIndices);
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			priorStatsIndices[0] = classIdx;
			
			// TODO from arguments
			final double classPriorSamples = 1.0;
			_classPrior.set(classIdx, classPriorSamples);

			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				priorStatsIndices[1] = channelIdx;
				final int priorStatsIdx = _classPriorStats.shape
						.calcFlatIndexFromLocation(priorStatsLoc);

				// TODO from arguments
				final double priorMean = 0.5 + rnd.nextDouble()*0.01;
				final double priorMeanSamples = 1.0;
				_classPriorStats.set(
						priorStatsIdx + PRIOR_STAT_MEAN, 
						priorMean);
				_classPriorStats.set(
						priorStatsIdx + PRIOR_STAT_NYU, 
						priorMeanSamples);

				// TODO from arguments
				final double priorVariance = Math.pow(1.0, 2);
				final double priorVarianceSamples = 1.0;
				_classPriorStats.set(
						priorStatsIdx + PRIOR_STAT_ALPHA, 
						priorVarianceSamples/2.0);
				_classPriorStats.set(
						priorStatsIdx + PRIOR_STAT_BETA,
						priorVariance*priorVarianceSamples/2.0);
			}
		}
	}
	
	public void clearObservations() {
		_classAdded.fill(0.0);
		_classAddedStats.fill(0.0);
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
		
		final Shape addedStatsShape = _classAddedStats.shape;
		final int[] addedStatsIndices = new int[addedStatsShape.ndim];
		final Location addedStatsLoc = new Location(addedStatsIndices);
		
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			addedStatsIndices[0] = classIdx;

			// get provided class probability
			final double classProb = classProbs[classProbsStart + classIdx];
			
			// add to class counts
			_classAdded.add(classIdx, classProb);
			
			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				addedStatsIndices[1] = channelIdx;

				// get provided channel value
				final double channelValue = channelData[channelDataStart + channelIdx];
				
				// calculate stats index
				final int startIdx = addedStatsShape.calcFlatIndexFromLocation(addedStatsLoc);
				
				// add to class stats
				_classAddedStats.add(startIdx + ADDED_STAT_SUM, classProb*channelValue);
				_classAddedStats.add(startIdx + ADDED_STAT_SUM_SQ, classProb*channelValue*channelValue);
				_classAddedStats.add(startIdx + ADDED_STAT_WEIGHT, classProb);
			}
		}
	}
	
	public void calculateClassProbs(
			final double[] classProbs,
			final int classProbsStart,
			final double[] channelData,
			final int channelDataStart) {

		if (classProbs == null) {
			throw new NullPointerException("classProbs");
		}
		if (channelData == null) {
			throw new NullPointerException("channelData");
		}
		
		final Shape priorStatsShape = _classPriorStats.shape;
		final int[] priorStatsIndices = new int[priorStatsShape.ndim];
		final Location priorStatsLoc = new Location(priorStatsIndices);
		
		final Shape addedStatsShape = _classAddedStats.shape;
		final int[] addedStatsIndices = new int[addedStatsShape.ndim];
		final Location addedStatsLoc = new Location(addedStatsIndices);
		
		// calculate posterior log likelihood
		for (int classIdx = 0; classIdx < _classCount; classIdx++) {
			
			priorStatsIndices[0] = classIdx;
			addedStatsIndices[0] = classIdx;
			
			// start accumulating from zero
			classProbs[classProbsStart + classIdx] = 0.0;
			
			// add likelihood from the arguments
			{
				classProbs[classProbsStart + classIdx] += 
						StatsUtils.checkFinite(
								Math.log(LOG_INSURANCE 
										+ classProbs[classProbsStart + classIdx]));
			}
			
			// add likelihood from class observations
			{
				final double priorSamples = _classPrior.get(classIdx);
				final double addedSamples = _classAdded.get(classIdx);
				classProbs[classProbsStart + classIdx] += 
						StatsUtils.checkFinite(
								Math.log(LOG_INSURANCE 
										+ priorSamples
										+ addedSamples));
			}
			
			// add class stats likelihoods
			for (int channelIdx = 0; channelIdx < _channelCount; channelIdx++) {
				
				priorStatsIndices[1] = channelIdx;
				addedStatsIndices[1] = channelIdx;
				
				final int priorStartIdx = priorStatsShape.calcFlatIndexFromLocation(priorStatsLoc);
				final int addedStartIdx = addedStatsShape.calcFlatIndexFromLocation(addedStatsLoc);
				
				final double priorMean = _classPriorStats.get(priorStartIdx + PRIOR_STAT_MEAN);
				final double priorNyu = _classPriorStats.get(priorStartIdx + PRIOR_STAT_NYU);
				final double priorAlpha = _classPriorStats.get(priorStartIdx + PRIOR_STAT_ALPHA);
				final double priorBeta = _classPriorStats.get(priorStartIdx + PRIOR_STAT_BETA);

				final double addedSum = _classAddedStats.get(addedStartIdx + ADDED_STAT_SUM);
				final double addedSumSq = _classAddedStats.get(addedStartIdx + ADDED_STAT_SUM_SQ);
				final double addedWeight = _classAddedStats.get(addedStartIdx + ADDED_STAT_WEIGHT);

				// calculate posterior nyu
				final double posteriorNyu = 
						priorNyu + addedWeight;
				
				// calculate posterior mean
				final double posteriorMean = 
						(priorNyu*priorMean + addedSum) /
						(priorNyu + addedWeight);

				// calculate posterior alpha
				final double posteriorAlpha = 
						priorAlpha + addedWeight / 2.0;

				// calculate posterior beta
				final double posteriorBeta;
				if (addedWeight > 0.0) {
					posteriorBeta = priorBeta 
							+ 0.5 * (addedSumSq - Math.pow(addedSum, 2)/addedWeight)
							+ 0.5 * (priorNyu*addedWeight) / (priorNyu + addedWeight) 
							* Math.pow(addedSum/addedWeight - priorMean, 2);
				} else {
					posteriorBeta = priorBeta;
				}

				// student log like params
				final double studentNyu = 2.0 * posteriorAlpha;
				final double studentMean = posteriorMean;
				final double studentSigma = 
						posteriorBeta * (posteriorNyu + 1.0) 
						/ posteriorAlpha 
						/ posteriorNyu;
				
				// get provided channel value
				final double channelValue = channelData[channelDataStart + channelIdx];

				// Student-t log like of channelValue
				classProbs[classProbsStart + classIdx] += 
						StatsUtils.checkFinite(
								StudentT.logLike(
										studentNyu, 
										studentMean, 
										studentSigma, 
										channelValue));
			}
		}
		
		// normalize to probabilities
		StatsUtils.logLikesToProbsInPlace(
				classProbs, 
				classProbsStart,
				_classCount);
	}
	
	public void calculateChannelMeans(
			final double[] classProbs,
			final int classProbsStart,
			final double[] channelData,
			final int channelDataStart) {

		if (classProbs == null) {
			throw new NullPointerException("classProbs");
		}
		if (channelData == null) {
			throw new NullPointerException("channelData");
		}
		
		final Shape priorStatsShape = _classPriorStats.shape;
		final int[] priorStatsIndices = new int[priorStatsShape.ndim];
		final Location priorStatsLoc = new Location(priorStatsIndices);
		
		final Shape addedStatsShape = _classAddedStats.shape;
		final int[] addedStatsIndices = new int[addedStatsShape.ndim];
		final Location addedStatsLoc = new Location(addedStatsIndices);

		// first reset means to zero
		for (int channelIdx = 0; channelIdx < _channelCount; channelIdx++) {
			channelData[channelDataStart + channelIdx] = 0.0;
		}
		
		// calculate posterior log likelihood
		for (int classIdx = 0; classIdx < _classCount; classIdx++) {
			
			priorStatsIndices[0] = classIdx;
			addedStatsIndices[0] = classIdx;
			
			// get provided class probability
			final double classProb = classProbs[classProbsStart + classIdx];

			// add class stats likelihoods
			for (int channelIdx = 0; channelIdx < _channelCount; channelIdx++) {
				
				priorStatsIndices[1] = channelIdx;
				addedStatsIndices[1] = channelIdx;
				
				final int priorStartIdx = priorStatsShape.calcFlatIndexFromLocation(priorStatsLoc);
				final int addedStartIdx = addedStatsShape.calcFlatIndexFromLocation(addedStatsLoc);
				
				final double priorMean = _classPriorStats.get(priorStartIdx + PRIOR_STAT_MEAN);
				final double priorNyu = _classPriorStats.get(priorStartIdx + PRIOR_STAT_NYU);

				final double addedSum = _classAddedStats.get(addedStartIdx + ADDED_STAT_SUM);
				final double addedWeight = _classAddedStats.get(addedStartIdx + ADDED_STAT_WEIGHT);
				
				// calculate posterior mean
				final double posteriorMean = 
						(priorNyu*priorMean + addedSum) /
						(priorNyu + addedWeight);

				// add to the output channel data
				channelData[channelDataStart + channelIdx] 
						+= classProb*posteriorMean;
			}
		}
	}
	

}
