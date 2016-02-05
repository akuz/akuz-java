package me.akuz.mnist.digits.visor.algo;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.StatsUtils;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;

/**
 * Gaussian multi-channel classes.
 *
 */
public final class GaussClasses2 {
	
	public static final int PARAM_MEAN = 0;
	public static final int PARAM_MEAN_SAMPLES = 1;
	public static final int PARAM_SIGMA = 2;
	public static final int PARAM_SIGMA_SAMPLES = 3;
	public static final int LENGTH_PARAM = 4;
	
	public static final int STAT_SUM = 0;
	public static final int STAT_SUM_SQ = 1;
	public static final int STAT_SAMPLES = 2;
	public static final int LENGTH_STAT = 3;

	private final int _classCount;
	private final int _channelCount;
	
	private final double _priorClassDPAlpha;
	private final Tensor _priorClassDPProbs;
	private final Tensor _priorChannelParams;

	private final Tensor _addedClassCount;
	private       double _addedClassCountSum;
	private final Tensor _addedChannelStats;

	private double _temperature;
	
	public GaussClasses2(
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
		
		// TODO from arguments
		_priorClassDPAlpha = 100.0 * _classCount;
		_priorClassDPProbs = new Tensor(new Shape(_classCount));
		_priorChannelParams = new Tensor(new Shape(_classCount, _channelCount, LENGTH_PARAM));
		
		// set class and class stats priors
		final Random rnd = ThreadLocalRandom.current();
		final int[] priorPerClassParamsIndices = new int[_priorChannelParams.ndim];
		final Location priorPerClassParamsLoc = new Location(priorPerClassParamsIndices);
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			priorPerClassParamsIndices[0] = classIdx;

			// TODO from arguments
			final double priorClassObs = 1.0 + rnd.nextDouble()*0.01;
			_priorClassDPProbs.set(classIdx, priorClassObs);

			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				priorPerClassParamsIndices[1] = channelIdx;
				final int priorPerClassParamsIdx = _priorChannelParams.shape
						.calcFlatIndexFromLocation(priorPerClassParamsLoc);

				// TODO from arguments
				final double priorMean = 0.5 + rnd.nextDouble()*0.01;
				final double priorMeanSamples = 10.0;
				_priorChannelParams.set(
						priorPerClassParamsIdx + PARAM_MEAN, 
						priorMean);
				_priorChannelParams.set(
						priorPerClassParamsIdx + PARAM_MEAN_SAMPLES, 
						priorMeanSamples);

				// TODO from arguments
				final double priorSigma = 1.0 + rnd.nextDouble()*0.01;
				final double priorSigmaSamples = 10.0;
				_priorChannelParams.set(
						priorPerClassParamsIdx + PARAM_SIGMA, 
						priorSigma);
				_priorChannelParams.set(
						priorPerClassParamsIdx + PARAM_SIGMA_SAMPLES,
						priorSigmaSamples);
			}
		}
		
		StatsUtils.normalizeInPlace(_priorClassDPProbs.data());

		_addedClassCount = new Tensor(new Shape(_classCount));
		_addedChannelStats = new Tensor(new Shape(_classCount, _channelCount, LENGTH_STAT));

		_temperature = 1.0;
	}
	
	public double getTemperature() {
		return _temperature;
	}
	
	public void setTemperature(final double temperature) {
		if (temperature < 0.0 || temperature > 1.0) {
			throw new IllegalArgumentException(
					"Temperature must be within interval " + 
					"[0.0, 1.0], but got " + temperature);
		}
		_temperature = temperature;
	}
	
	public void iterate(
			final double[] classProbs,
			final int classProbsStart,
			final double[] channelData,
			final int channelDataStart)
	{
		// remove the provided observation
		processObservation(-1.0, classProbs, classProbsStart, channelData, channelDataStart);
		
		// compute class assignment log-likelihoods
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			// compute absolute index
			final int classProbsIdx = classProbsStart + classIdx;

			// reset for log-likelihood calculation
			classProbs[classProbsIdx] = 0.0;
			
			// TODO
		}

		// normalize computed log-likelihoods to probabilities
		StatsUtils.logLikesToProbsInPlace(classProbs, classProbsStart, _classCount);

		// add the computed observation
		processObservation(1.0, classProbs, classProbsStart, channelData, channelDataStart);
	}
	
	private void processObservation(
			final double multiplier,
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
		
		final Shape addedStatsShape = _addedChannelStats.shape;
		final int[] addedStatsIndices = new int[addedStatsShape.ndim];
		final Location addedStatsLoc = new Location(addedStatsIndices);
		
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			addedStatsIndices[0] = classIdx;

			// get provided class probability
			final double classProb = classProbs[classProbsStart + classIdx];
			
			// add to class counts
			_addedClassCount.add(classIdx, multiplier*classProb);
			_addedClassCountSum += multiplier*classProb;
			
			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				addedStatsIndices[1] = channelIdx;

				// get provided channel value
				final double channelValue = channelData[channelDataStart + channelIdx];
				
				// calculate stats index
				final int startIdx = addedStatsShape.calcFlatIndexFromLocation(addedStatsLoc);
				
				// add to class stats
				_addedChannelStats.add(startIdx + STAT_SUM, multiplier*classProb*channelValue);
				_addedChannelStats.add(startIdx + STAT_SUM_SQ, multiplier*classProb*channelValue*channelValue);
				_addedChannelStats.add(startIdx + STAT_SAMPLES, multiplier*classProb);
			}
		}
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
		
		final Shape priorStatsShape = _priorChannelParams.shape;
		final int[] priorStatsIndices = new int[priorStatsShape.ndim];
		final Location priorStatsLoc = new Location(priorStatsIndices);
		
		final Shape addedStatsShape = _addedChannelStats.shape;
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
				
				final double priorMean = _priorChannelParams.get(priorStartIdx + PARAM_MEAN);
				final double priorMeanSamples = _priorChannelParams.get(priorStartIdx + PARAM_MEAN_SAMPLES);

				final double addedSum = _addedChannelStats.get(addedStartIdx + STAT_SUM);
				final double addedSamples = _addedChannelStats.get(addedStartIdx + STAT_SAMPLES);
				
				// calculate posterior mean
				final double posteriorMean = 
						(priorMeanSamples*priorMean + addedSum) /
						(priorMeanSamples + addedSamples);

				// add to the output channel data
				channelData[channelDataStart + channelIdx] 
						+= classProb*posteriorMean;
			}
		}
	}

}
