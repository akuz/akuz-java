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

	public static final int META_CLASS_PRIOR_DP_ALPHA = 0;
	public static final int META_CLASS_PRIOR_DP_LOG_NORM = 1;
	public static final int META_CLASS_ADDED_SAMPLES_SUM = 2;
	public static final int META_CLASS_STATS_COUNT = 3;

	public static final int DEEP_CLASS_PRIOR_DP_PROB = 0;
	public static final int DEEP_CLASS_ADDED_SAMPLES = 1;
	public static final int DEEP_CLASS_STATS_COUNT = 2;

	public static final int META_CHANNEL_PRIOR_DP_ALPHA = 0;
	public static final int META_CHANNEL_PRIOR_DP_LOG_NORM = 1;
	public static final int META_CHANNEL_ADDED_SAMPLES_SUM = 2;
	public static final int META_CHANNEL_STATS_COUNT = 3;

	public static final int DEEP_CHANNEL_PRIOR_DP_PROB = 0;
	public static final int DEEP_CHANNEL_ADDED_SAMPLES = 1;
	public static final int DEEP_CHANNEL_STATS_COUNT = 2;

	private final int _classCount;
	private final int _channelCount;
	private final int _channelDimCount;

	private final Tensor _metaClassData;
	private final Tensor _deepClassData;
	private final Tensor _metaChannelData;
	private final Tensor _deepChannelData;

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
		
		_metaClassData = new Tensor(new Shape(META_CLASS_STATS_COUNT));
		_deepClassData = new Tensor(new Shape(_classCount, DEEP_CLASS_STATS_COUNT));
		_metaChannelData = new Tensor(new Shape(_classCount, _channelCount, META_CHANNEL_STATS_COUNT));
		_deepChannelData = new Tensor(new Shape(_classCount, _channelCount, _channelDimCount, DEEP_CHANNEL_STATS_COUNT));
		
		final int[] deepClassIndices = new int[_deepClassData.ndim];
		final Location deepClassLoc = new Location(deepClassIndices);
		final int[] metaChannelIndices = new int[_metaChannelData.ndim];
		final Location metaChannelLoc = new Location(metaChannelIndices);
		final int[] deepChannelIndices = new int[_deepChannelData.ndim];
		final Location deepChannelLoc = new Location(deepChannelIndices);

		// reused arrays
		final double[] deepClassPriorDPProbs = new double[_classCount];
		final double[] deepChannelPriorDPProbs = new double[_channelDimCount];

		// initialize deep-class base distribution
		final DPMetaInfo classDPMeta;
		{
			// TODO from arguments
			final double classPriorDPAlpha = 10.0;
			classDPMeta = DPFunctions.initDP(
					classPriorDPAlpha,
					deepClassPriorDPProbs,
					0, 
					deepClassPriorDPProbs.length);
		}

		_metaClassData.set(
				META_CLASS_PRIOR_DP_ALPHA, 
				classDPMeta.alpha());

		_metaClassData.set(
				META_CLASS_PRIOR_DP_LOG_NORM, 
				classDPMeta.logNorm());

		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			deepClassIndices[0] = classIdx;
			metaChannelIndices[0] = classIdx;
			deepChannelIndices[0] = classIdx;
			
			final int deepClassDataIdx = _deepClassData.shape
					.calcFlatIndexFromLocation(deepClassLoc);

			_deepClassData.set(
					deepClassDataIdx + DEEP_CLASS_PRIOR_DP_PROB, 
					deepClassPriorDPProbs[classIdx]);

			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {

				metaChannelIndices[1] = channelIdx;
				deepChannelIndices[1] = channelIdx;

				final int metaChannelDataIdx = _metaChannelData.shape
						.calcFlatIndexFromLocation(metaChannelLoc);

				// initialize deep-channel base distribution
				final DPMetaInfo channelDPMeta;
				{
					// TODO: from arguments
					final double channelPriorDPAlpha = 10.0;
					channelDPMeta = DPFunctions.initDP(
							channelPriorDPAlpha,
							deepChannelPriorDPProbs,
							0,
							deepChannelPriorDPProbs.length);
				}

				_metaChannelData.set(
						metaChannelDataIdx + META_CHANNEL_PRIOR_DP_ALPHA,
						channelDPMeta.alpha());

				_metaChannelData.set(
						metaChannelDataIdx + META_CHANNEL_PRIOR_DP_LOG_NORM,
						channelDPMeta.logNorm());

				for (int dimIdx=0; dimIdx<_channelDimCount; dimIdx++) {
					
					deepChannelIndices[2] = dimIdx;
					
					final int deepChannelDataIdx = _deepChannelData.shape
							.calcFlatIndexFromLocation(deepChannelLoc);

					_deepChannelData.set(
							deepChannelDataIdx + DEEP_CHANNEL_PRIOR_DP_PROB, 
							deepChannelPriorDPProbs[dimIdx]);
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
			final double[] channelData,
			final int channelDataStart)
	{
		// remove the provided observation
		processObservation(-1.0, classProbs, classProbsStart, channelData, channelDataStart);
		
		final int[] deepClassIndices = new int[_deepClassData.ndim];
		final Location deepClassLoc = new Location(deepClassIndices);
		final int[] metaChannelIndices = new int[_metaChannelData.ndim];
		final Location metaChannelLoc = new Location(metaChannelIndices);
		final int[] deepChannelIndices = new int[_deepChannelData.ndim];
		final Location deepChannelLoc = new Location(deepChannelIndices);
		
		// would-be total number of class samples
		final double wouldbeClassSamplesSum = 1.0 +
				_metaClassData.get(META_CHANNEL_ADDED_SAMPLES_SUM);

		// compute class assignment log-likelihoods
		final double metaClassDPAlpha = _metaClassData.get(META_CLASS_PRIOR_DP_ALPHA);
		final double metaClassDPLogNorm = _metaClassData.get(META_CLASS_PRIOR_DP_LOG_NORM);
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			// log-likelihood population index
			final int logLikeIdx = classProbsStart + classIdx;
			
			// init log-likelihood calculation
			// with class DP normalization constant
			classProbs[logLikeIdx] = metaClassDPLogNorm;
			
			// class distribution log-likelihood
			for (int newClassIdx=0; newClassIdx<_classCount; newClassIdx++) {

				deepClassIndices[0] = newClassIdx;
				metaChannelIndices[0] = newClassIdx;
				deepChannelIndices[0] = newClassIdx;

				final int deepClassDataIdx = _deepClassData.shape
						.calcFlatIndexFromLocation(deepClassLoc);

				// get prior class info
				final double deepClassPriorDPProb = _deepClassData.get(deepClassDataIdx + DEEP_CLASS_PRIOR_DP_PROB);
				final double deepClassPriorDPProbAlpha = metaClassDPAlpha * deepClassPriorDPProb;
				
				// would-be the number of samples of this class
				final double wouldbeClassSamples = 1.0 +
						_deepClassData.get(deepClassDataIdx + DEEP_CLASS_ADDED_SAMPLES);
				
				// would-be sample probability of this class
				final double wouldbeClassProb =
						wouldbeClassSamples /
						wouldbeClassSamplesSum;
				
				// apply simulated annealing
				final double annealedClassProb =
						_temperature * deepClassPriorDPProb +
						(1.0 - _temperature) * wouldbeClassProb;
				
				// add log-likelihood
				classProbs[logLikeIdx] += 
						(deepClassPriorDPProbAlpha - 1.0) * 
						Math.log(annealedClassProb);
				
				// channels distribution log-likelihood
				for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
					
//					final double channelPriorMean = _channelData.get
					// TODO
				}
			}
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
		
		final int[] classIndices = new int[_metaChannelData.ndim];
		final Location classLoc = new Location(classIndices);
		
		final int[] channelIndices = new int[_metaChannelData.ndim];
		final Location channelLoc = new Location(channelIndices);
		
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			classIndices[0] = classIdx;
			channelIndices[0] = classIdx;
			
			final int classDataIdx = _deepClassData.shape.calcFlatIndexFromLocation(classLoc);

			// get provided class probability
			final double classProb = classProbs[classProbsStart + classIdx];
			
			// class stats
			_deepClassData.add(
					classDataIdx + DEEP_CLASS_ADDED_SAMPLES, 
					multiplier*classProb);
			_classAddedSamplesSum += 
					multiplier*classProb;
			
			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				channelIndices[1] = channelIdx;
				
				final int channelDataIdx = _metaChannelData.shape.calcFlatIndexFromLocation(channelLoc);

				// get provided channel value
				final double channelValue = channelData[channelDataStart + channelIdx];
				
				// channel stats
				_metaChannelData.add(
						channelDataIdx + CHANNEL_ADDED_SUM, 
						multiplier*classProb*channelValue);
				_metaChannelData.add(
						channelDataIdx + CHANNEL_ADDED_SUM_SQ, 
						multiplier*classProb*channelValue*channelValue);
				_metaChannelData.add(
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
		
		final int[] channelIndices = new int[_metaChannelData.ndim];
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
				
				final int channelDataIdx = _metaChannelData.shape.calcFlatIndexFromLocation(channelLoc);
				
				final double priorMean = _metaChannelData.get(channelDataIdx + CHANNEL_PRIOR_MEAN);
				final double priorMeanSamples = _metaChannelData.get(channelDataIdx + CHANNEL_PRIOR_MEAN_SAMPLES);

				final double addedSum = _metaChannelData.get(channelDataIdx + CHANNEL_ADDED_SUM);
				final double addedSamples = _metaChannelData.get(channelDataIdx + CHANNEL_ADDED_SAMPLES);
				
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
