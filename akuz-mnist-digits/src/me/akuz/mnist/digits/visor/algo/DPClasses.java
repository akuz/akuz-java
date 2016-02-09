package me.akuz.mnist.digits.visor.algo;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.GammaFunction;
import me.akuz.core.math.StatsUtils;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;

/**
 * Gaussian multi-channel classes.
 *
 */
public final class DPClasses {

	public static final int CLASS_PRIOR_DP_ALPHA = 0;
	public static final int CLASS_PRIOR_DP_LOG_NORM = 1;
	public static final int CLASS_ADDED_SAMPLES_SUM = 2;
	public static final int CLASS_STATS_COUNT = 3;

	public static final int CLASS_DIM_PRIOR_DP_PROB = 0;
	public static final int CLASS_DIM_ADDED_SAMPLES = 1;
	public static final int CLASS_DIM_STATS_COUNT = 2;

	public static final int CLASS_CHANNEL_PRIOR_DP_ALPHA = 0;
	public static final int CLASS_CHANNEL_PRIOR_DP_LOG_NORM = 1;
	public static final int CLASS_CHANNEL_ADDED_SAMPLES_SUM = 2;
	public static final int CLASS_CHANNEL_STATS_COUNT = 3;

	public static final int CLASS_CHANNEL_DIM_PRIOR_DP_PROB = 0;
	public static final int CLASS_CHANNEL_DIM_ADDED_SAMPLES = 1;
	public static final int CLASS_CHANNEL_DIM_STATS_COUNT = 2;

	private final int _classCount;
	private final int _channelCount;
	private final int _channelDimCount;

	private final Tensor _classData;
	private final Tensor _classDimData;
	private final Tensor _classChannelData;
	private final Tensor _classChannelDimData;

	private double _temperature;
	
	public DPClasses(
			final int classCount,
			final int channelCount,
			final int channelDimCount) {
		
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
		
		if (channelDimCount < 2) {
			throw new IllegalArgumentException(
					"channelDimCount must be >= 2, got " + 
					channelDimCount);
		}
		_channelDimCount = channelDimCount;
		
		_classData = new Tensor(new Shape(CLASS_STATS_COUNT));
		_classDimData = new Tensor(new Shape(_classCount, CLASS_DIM_STATS_COUNT));
		_classChannelData = new Tensor(new Shape(_classCount, _channelCount, CLASS_CHANNEL_STATS_COUNT));
		_classChannelDimData = new Tensor(new Shape(_classCount, _channelCount, _channelDimCount, CLASS_CHANNEL_DIM_STATS_COUNT));
		
		final int[] classDimIndices = new int[_classDimData.ndim];
		final Location classDimLoc = new Location(classDimIndices);
		final int[] classChannelIndices = new int[_classChannelData.ndim];
		final Location classChannelLoc = new Location(classChannelIndices);
		final int[] classChannelDimIndices = new int[_classChannelDimData.ndim];
		final Location classChannelDimLoc = new Location(classChannelDimIndices);

		// reused arrays
		final double[] classDimPriorDPProbs = new double[_classCount];
		final double[] channelDimPriorDPProbs = new double[_channelDimCount];

		// initialize deep-class base distribution
		final DPMetaInfo classDPMeta;
		{
			// TODO from arguments
			final double classPriorDPAlpha = 10.0;
			classDPMeta = DPFunctions.initDP(
					classPriorDPAlpha,
					classDimPriorDPProbs,
					0, 
					classDimPriorDPProbs.length);
		}

		_classData.set(
				CLASS_PRIOR_DP_ALPHA, 
				classDPMeta.alpha());

		_classData.set(
				CLASS_PRIOR_DP_LOG_NORM, 
				classDPMeta.logNorm());

		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			classDimIndices[0] = classIdx;
			classChannelIndices[0] = classIdx;
			classChannelDimIndices[0] = classIdx;
			
			final int classDimDataIdx = _classDimData.shape
					.calcFlatIndexFromLocation(classDimLoc);

			_classDimData.set(
					classDimDataIdx + CLASS_DIM_PRIOR_DP_PROB, 
					classDimPriorDPProbs[classIdx]);

			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {

				classChannelIndices[1] = channelIdx;
				classChannelDimIndices[1] = channelIdx;

				final int classChannelDataIdx = _classChannelData.shape
						.calcFlatIndexFromLocation(classChannelLoc);

				// initialize deep-channel base distribution
				final DPMetaInfo channelDPMeta;
				{
					// TODO: from arguments
					final double channelPriorDPAlpha = 10.0;
					channelDPMeta = DPFunctions.initDP(
							channelPriorDPAlpha,
							channelDimPriorDPProbs,
							0,
							channelDimPriorDPProbs.length);
				}

				_classChannelData.set(
						classChannelDataIdx + CLASS_CHANNEL_PRIOR_DP_ALPHA,
						channelDPMeta.alpha());

				_classChannelData.set(
						classChannelDataIdx + CLASS_CHANNEL_PRIOR_DP_LOG_NORM,
						channelDPMeta.logNorm());

				for (int dimIdx=0; dimIdx<_channelDimCount; dimIdx++) {
					
					classChannelDimIndices[2] = dimIdx;
					
					final int classChannelDimDataIdx = _classChannelDimData.shape
							.calcFlatIndexFromLocation(classChannelDimLoc);

					_classChannelDimData.set(
							classChannelDimDataIdx + CLASS_CHANNEL_DIM_PRIOR_DP_PROB, 
							channelDimPriorDPProbs[dimIdx]);
				}
			}
		}

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
			final double[] channelDimData,
			final int channelDimDataStart) {
		
		// remove the provided observation
		processObservation(-1.0, classProbs, classProbsStart, channelDimData, channelDimDataStart);
		
		// for iterating through stats
		final int[] classDimIndices = new int[_classDimData.ndim];
		final Location classDimLoc = new Location(classDimIndices);
		final int[] classChannelIndices = new int[_classChannelData.ndim];
		final Location classChannelLoc = new Location(classChannelIndices);
		final int[] classChannelDimIndices = new int[_classChannelDimData.ndim];
		final Location classChannelDimLoc = new Location(classChannelDimIndices);
		
		// for iterating through channel data
		final Shape channelDimDataShape = new Shape(_channelCount, _channelDimCount);
		final int[] channelDimDataIndices = new int[2];
		final Location channelDimDataLoc = new Location(channelDimDataIndices);

		// meta class information
		final double classPriorDPAlpha = 
				_classData.get(CLASS_PRIOR_DP_ALPHA);
		final double classPriorDPLogNorm = 
				_classData.get(CLASS_PRIOR_DP_LOG_NORM);
		final double classAddedSamplesSum =
				_classData.get(CLASS_ADDED_SAMPLES_SUM) + 1.0;
		
		// compute class assignment log-likelihoods
		for (int newClassIdx=0; newClassIdx<_classCount; newClassIdx++) {
			
			// log-likelihood population index
			final int classProbsIdx = classProbsStart + newClassIdx;

			// init log-likelihood calculation
			// with class DP normalization constant
			classProbs[classProbsIdx] = classPriorDPLogNorm;

			classDimIndices[0] = newClassIdx;
			classChannelIndices[0] = newClassIdx;
			classChannelDimIndices[0] = newClassIdx;

			final int classDimDataIdx = _classDimData.shape
					.calcFlatIndexFromLocation(classDimLoc);

			// get prior class info
			final double classDimPriorDPProb = 
					_classDimData.get(classDimDataIdx + 
							CLASS_DIM_PRIOR_DP_PROB);
			final double classDimPriorDPProbAlpha = 
					classDimPriorDPProb * classPriorDPAlpha;
			
			// would-be the number of samples of this class
			final double classDimAddedSamples =
					_classDimData.get(classDimDataIdx + 
							CLASS_DIM_ADDED_SAMPLES) + 1.0;
			
			// would-be sample probability of this class
			final double classSampleProb =
					classDimAddedSamples /
					classAddedSamplesSum;
			
			// apply simulated annealing
			final double annealedClassProb =
					_temperature * classDimPriorDPProb +
					(1.0 - _temperature) * classSampleProb;
			
			// add DP log-likelihood
			classProbs[classProbsIdx] += 
					(classDimPriorDPProbAlpha - 1.0) * 
					Math.log(annealedClassProb);
			
			// channels distribution log-likelihood
			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				classChannelIndices[1] = channelIdx;
				classChannelDimIndices[1] = channelIdx;
				channelDimDataIndices[0] = channelIdx;
				
				final int classChannelDataIdx = _classChannelData.shape
						.calcFlatIndexFromLocation(classChannelLoc);

				// meta channel information
				final double classChannelPriorDPAlpha = 
						_classChannelData.get(classChannelDataIdx + 
								CLASS_CHANNEL_PRIOR_DP_ALPHA);
				final double classChannelPriorDPLogNorm = 
						_classChannelData.get(classChannelDataIdx + 
								CLASS_CHANNEL_PRIOR_DP_LOG_NORM);
				final double classChannelAddedSamplesSum =
						_classChannelData.get(classChannelDataIdx + 
								CLASS_CHANNEL_ADDED_SAMPLES_SUM) + 1.0;

				// add class DP normalization constant
				classProbs[classProbsIdx] += classChannelPriorDPLogNorm;

				// channel distribution log-likelihood
				for (int channelDimIdx=0; channelDimIdx<_channelDimCount; channelDimIdx++) {

					classChannelDimIndices[2] = channelDimIdx;
					channelDimDataIndices[1] = channelDimIdx;

					final int classChannelDimDataIdx = _classChannelDimData.shape
							.calcFlatIndexFromLocation(classChannelDimLoc);

					final int channelDimDataIdx = channelDimDataShape
							.calcFlatIndexFromLocation(channelDimDataLoc);
					
					final double channelDimDataValue =
							channelDimData[channelDimDataStart + channelDimDataIdx];

					// get prior info for dim
					final double classChannelDimPriorDPProb = 
							_classChannelDimData.get(classChannelDimDataIdx + 
									CLASS_CHANNEL_DIM_PRIOR_DP_PROB);
					final double classChannelDimPriorDPProbAlpha = 
							classChannelDimPriorDPProb * classChannelPriorDPAlpha;
					
					// would-be the number of samples of this dim
					final double classChannelDimAddedSamples = 
							_classChannelDimData.get(classChannelDimDataIdx + 
									CLASS_CHANNEL_DIM_ADDED_SAMPLES) + channelDimDataValue;
					
					// would-be sample probability of this dim
					final double classChannelDimSampleProb =
							classChannelDimAddedSamples /
							classChannelAddedSamplesSum;
					
					// apply simulated annealing
					final double annealedClassChannelDimProb =
							_temperature * classChannelDimPriorDPProb +
							(1.0 - _temperature) * classChannelDimSampleProb;
					
					// add log-likelihood
					classProbs[classProbsIdx] += 
							(classChannelDimPriorDPProbAlpha - 1.0) * 
							Math.log(annealedClassChannelDimProb);
				}
			}
		}

		// normalize computed log-likelihoods to probabilities
		StatsUtils.logLikesToProbsInPlace(classProbs, classProbsStart, _classCount);

		// add the computed observation
		processObservation(1.0, classProbs, classProbsStart, channelDimData, channelDimDataStart);
	}
	
	private void processObservation(
			final double multiplier,
			final double[] classProbs,
			final int classProbsStart,
			final double[] channelDimData,
			final int channelDimDataStart) {
		
		if (classProbs == null) {
			throw new NullPointerException("classWeights");
		}
		if (channelDimData == null) {
			throw new NullPointerException("channelDimData");
		}
		
		final int[] classIndices = new int[_classChannelData.ndim];
		final Location classLoc = new Location(classIndices);
		
		final int[] channelIndices = new int[_classChannelData.ndim];
		final Location channelLoc = new Location(channelIndices);
		
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			classIndices[0] = classIdx;
			channelIndices[0] = classIdx;
			
			final int classDataIdx = _classDimData.shape
					.calcFlatIndexFromLocation(classLoc);

			// get provided class probability
			final double classProb = classProbs[classProbsStart + classIdx];
			
			// class stats
			_classDimData.add(
					classDataIdx + CLASS_DIM_ADDED_SAMPLES, 
					multiplier*classProb);
			_classAddedSamplesSum += 
					multiplier*classProb;
			
			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				channelIndices[1] = channelIdx;
				
				final int channelDataIdx = _classChannelData.shape
						.calcFlatIndexFromLocation(channelLoc);

				// get provided channel value
				final double channelValue = channelDimData[channelDimDataStart + channelIdx];
				
				// channel stats
				_classChannelData.add(
						channelDataIdx + CHANNEL_ADDED_SUM, 
						multiplier*classProb*channelValue);
				_classChannelData.add(
						channelDataIdx + CHANNEL_ADDED_SUM_SQ, 
						multiplier*classProb*channelValue*channelValue);
				_classChannelData.add(
						channelDataIdx + CHANNEL_ADDED_SAMPLES,
						multiplier*classProb);
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
		
		final int[] channelIndices = new int[_classChannelData.ndim];
		final Location channelLoc = new Location(channelIndices);

		// first reset means to zero
		for (int channelIdx = 0; channelIdx < _channelCount; channelIdx++) {
			channelData[channelDataStart + channelIdx] = 0.0;
		}
		
		// calculate posterior log likelihood
		for (int classIdx = 0; classIdx < _classCount; classIdx++) {
			
			channelIndices[0] = classIdx;
			
			// get provided class probability
			final double classProb = classProbs[classProbsStart + classIdx];

			// add class stats likelihoods
			for (int channelIdx = 0; channelIdx < _channelCount; channelIdx++) {
				
				channelIndices[1] = channelIdx;
				
				final int channelDataIdx = _classChannelData.shape.calcFlatIndexFromLocation(channelLoc);
				
				final double priorMean = _classChannelData.get(channelDataIdx + CHANNEL_PRIOR_MEAN);
				final double priorMeanSamples = _classChannelData.get(channelDataIdx + CHANNEL_PRIOR_MEAN_SAMPLES);

				final double addedSum = _classChannelData.get(channelDataIdx + CHANNEL_ADDED_SUM);
				final double addedSamples = _classChannelData.get(channelDataIdx + CHANNEL_ADDED_SAMPLES);
				
				// TODO: use temperature
				
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
