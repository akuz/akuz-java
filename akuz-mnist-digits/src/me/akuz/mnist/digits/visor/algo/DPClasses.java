package me.akuz.mnist.digits.visor.algo;

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
			final double[] channelData,
			final int[] channelDataStarts) {
		
		if (classProbs == null) {
			throw new NullPointerException("classWeights");
		}
		if (channelData == null) {
			throw new NullPointerException("channelData");
		}
		if (channelDataStarts == null) {
			throw new NullPointerException("channelDataStarts");
		}
		if (channelDataStarts.length != _channelCount) {
			throw new IllegalArgumentException(
					"Expected channelDataStarts length of " +
					_channelCount + ", but got " + channelDataStarts.length);
		}

		// remove the provided observation
		processObservation(-1.0, classProbs, classProbsStart, channelData, channelDataStarts);
		
		// for iterating through stats
		final int[] classDimIndices = new int[_classDimData.ndim];
		final Location classDimLoc = new Location(classDimIndices);
		final int[] classChannelIndices = new int[_classChannelData.ndim];
		final Location classChannelLoc = new Location(classChannelIndices);
		final int[] classChannelDimIndices = new int[_classChannelDimData.ndim];
		final Location classChannelDimLoc = new Location(classChannelDimIndices);

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

				final int channelDataStart = channelDataStarts[channelIdx];

				// channel distribution log-likelihood
				for (int dimIdx=0; dimIdx<_channelDimCount; dimIdx++) {

					classChannelDimIndices[2] = dimIdx;

					final int classChannelDimDataIdx = _classChannelDimData.shape
							.calcFlatIndexFromLocation(classChannelDimLoc);

					final double channelDimValue =
							channelData[channelDataStart + dimIdx];

					// get prior info for dim
					final double classChannelDimPriorDPProb = 
							_classChannelDimData.get(classChannelDimDataIdx + 
									CLASS_CHANNEL_DIM_PRIOR_DP_PROB);
					final double classChannelDimPriorDPProbAlpha = 
							classChannelDimPriorDPProb * classChannelPriorDPAlpha;
					
					// would-be the number of samples of this dim
					final double classChannelDimAddedSamples = 
							_classChannelDimData.get(classChannelDimDataIdx + 
									CLASS_CHANNEL_DIM_ADDED_SAMPLES) + channelDimValue;
					
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
		processObservation(1.0, classProbs, classProbsStart, channelData, channelDataStarts);
	}
	
	private void processObservation(
			final double multiplier,
			final double[] classProbs,
			final int classProbsStart,
			final double[] channelData,
			final int[] channelDataStarts) {
		
		// for iterating through stats
		final int[] classDimIndices = new int[_classDimData.ndim];
		final Location classDimLoc = new Location(classDimIndices);
		final int[] classChannelIndices = new int[_classChannelData.ndim];
		final Location classChannelLoc = new Location(classChannelIndices);
		final int[] classChannelDimIndices = new int[_classChannelDimData.ndim];
		final Location classChannelDimLoc = new Location(classChannelDimIndices);
		
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			classDimIndices[0] = classIdx;
			classChannelIndices[0] = classIdx;
			classChannelDimIndices[0] = classIdx;
			
			final int classDimDataIdx = _classDimData.shape
					.calcFlatIndexFromLocation(classDimLoc);

			// get provided class probability
			final double classProb = classProbs[classProbsStart + classIdx];
			final double multipliedClassProb = multiplier*classProb;
			
			_classData.add(
					CLASS_ADDED_SAMPLES_SUM, 
					multipliedClassProb);
			
			_classDimData.add(
					classDimDataIdx + CLASS_DIM_ADDED_SAMPLES, 
					multipliedClassProb);
			
			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				classChannelIndices[1] = channelIdx;
				classChannelDimIndices[1] = channelIdx;
				
				final int classChannelDataIdx = _classChannelData.shape
						.calcFlatIndexFromLocation(classChannelLoc);
				
				_classChannelData.add(
						classChannelDataIdx + CLASS_CHANNEL_ADDED_SAMPLES_SUM,
						multipliedClassProb);
				
				final int channelDataStart = channelDataStarts[channelIdx];
				
				for (int dimIdx=0; dimIdx<_channelDimCount; dimIdx++) {
					
					classChannelDimIndices[2] = dimIdx;

					// get provided channel value
					final double channelDimValue = 
							channelData[channelDataStart + dimIdx];
					
					final int classChannelDimDataIdx = _classChannelDimData.shape
							.calcFlatIndexFromLocation(classChannelDimLoc);
					
					_classChannelDimData.add(
							classChannelDimDataIdx + CLASS_CHANNEL_DIM_ADDED_SAMPLES,
							multipliedClassProb*channelDimValue);
				}
			}
		}
	}
	
	public void calculateChannelMeans(
			final double[] classProbs,
			final int classProbsStart,
			final double[] channelData,
			final int[] channelDataStarts) {

		if (classProbs == null) {
			throw new NullPointerException("classWeights");
		}
		if (channelData == null) {
			throw new NullPointerException("channelData");
		}
		if (channelDataStarts == null) {
			throw new NullPointerException("channelDataStarts");
		}
		if (channelDataStarts.length != _channelCount) {
			throw new IllegalArgumentException(
					"Expected channelDataStarts length of " +
					_channelCount + ", but got " + channelDataStarts.length);
		}

		// for iterating through stats
		final int[] classChannelIndices = new int[_classChannelData.ndim];
		final Location classChannelLoc = new Location(classChannelIndices);
		final int[] classChannelDimIndices = new int[_classChannelDimData.ndim];
		final Location classChannelDimLoc = new Location(classChannelDimIndices);
		
		// compute class assignment log-likelihoods
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			final double classProb = classProbs[classProbsStart + classIdx];

			classChannelIndices[0] = classIdx;
			classChannelDimIndices[0] = classIdx;
			
			// channels distribution log-likelihood
			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				classChannelIndices[1] = channelIdx;
				classChannelDimIndices[1] = channelIdx;
				
				final int classChannelDataIdx = _classChannelData.shape
						.calcFlatIndexFromLocation(classChannelLoc);

				// meta channel information
				final double classChannelAddedSamplesSum =
						_classChannelData.get(classChannelDataIdx + 
								CLASS_CHANNEL_ADDED_SAMPLES_SUM);

				final int channelDataStart = channelDataStarts[channelIdx];

				// channel distribution log-likelihood
				for (int dimIdx=0; dimIdx<_channelDimCount; dimIdx++) {

					classChannelDimIndices[2] = dimIdx;

					final int classChannelDimDataIdx = _classChannelDimData.shape
							.calcFlatIndexFromLocation(classChannelDimLoc);

					// get prior info for dim
					final double classChannelDimPriorDPProb = 
							_classChannelDimData.get(classChannelDimDataIdx + 
									CLASS_CHANNEL_DIM_PRIOR_DP_PROB);
					
					// would-be the number of samples of this dim
					final double classChannelDimAddedSamples = 
							_classChannelDimData.get(classChannelDimDataIdx + 
									CLASS_CHANNEL_DIM_ADDED_SAMPLES);
					
					// would-be sample probability of this dim
					final double classChannelDimSampleProb =
							classChannelDimAddedSamples /
							classChannelAddedSamplesSum;
					
					// apply simulated annealing
					final double annealedClassChannelDimProb =
							_temperature * classChannelDimPriorDPProb +
							(1.0 - _temperature) * classChannelDimSampleProb;
					
					if (classIdx == 0) {
						channelData[channelDataStart + dimIdx] = annealedClassChannelDimProb * classProb;
					} else {
						channelData[channelDataStart + dimIdx] += annealedClassChannelDimProb * classProb;
					}
				}
			}
		}

	}

}
