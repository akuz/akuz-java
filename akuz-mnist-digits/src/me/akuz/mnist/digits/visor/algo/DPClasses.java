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

	public static final int CLASS_PRIOR_DP_LOG_NORM = 0;
	public static final int CLASS_ADDED_SAMPLES_SUM = 1;
	public static final int CLASS_STATS_COUNT = 2;

	public static final int CLASS_DIM_PRIOR_DP_ALPHA_PROB = 0;
	public static final int CLASS_DIM_ADDED_SAMPLES = 1;
	public static final int CLASS_DIM_STATS_COUNT = 2;

	public static final int CLASS_CHANNEL_PRIOR_DP_LOG_NORM = 0;
	public static final int CLASS_CHANNEL_ADDED_SAMPLES_SUM = 1;
	public static final int CLASS_CHANNEL_STATS_COUNT = 2;

	public static final int CLASS_CHANNEL_DIM_PRIOR_DP_ALPHA_PROB = 0;
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
		final double[] classDimPriorDPAlphaProbs = new double[_classCount];
		final double[] channelDimPriorDPAlphaProbs = new double[_channelDimCount];

		// initialize deep-class base distribution
		final double classPriorDPLogNorm;
		{
			// TODO from arguments
			final double classPriorDPAlpha = 1.0;
			classPriorDPLogNorm = 
					DPFunctions.initNoisyFlatDP(
							classPriorDPAlpha,
							classDimPriorDPAlphaProbs,
							0, 
							classDimPriorDPAlphaProbs.length);
		}

		_classData.set(
				CLASS_PRIOR_DP_LOG_NORM, 
				classPriorDPLogNorm);

		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			classDimIndices[0] = classIdx;
			classChannelIndices[0] = classIdx;
			classChannelDimIndices[0] = classIdx;
			
			final int classDimDataIdx = _classDimData.shape
					.calcFlatIndexFromLocation(classDimLoc);

			_classDimData.set(
					classDimDataIdx + CLASS_DIM_PRIOR_DP_ALPHA_PROB, 
					classDimPriorDPAlphaProbs[classIdx]);

			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {

				classChannelIndices[1] = channelIdx;
				classChannelDimIndices[1] = channelIdx;

				final int classChannelDataIdx = _classChannelData.shape
						.calcFlatIndexFromLocation(classChannelLoc);

				// initialize deep-channel base distribution
				final double channelPriorDPLogNorm;
				{
					// TODO: from arguments
					final double channelPriorDPAlpha = 0.1;
					channelPriorDPLogNorm = 
							DPFunctions.initNoisyFlatDP(
									channelPriorDPAlpha,
									channelDimPriorDPAlphaProbs,
									0,
									channelDimPriorDPAlphaProbs.length);
				}

				_classChannelData.set(
						classChannelDataIdx + CLASS_CHANNEL_PRIOR_DP_LOG_NORM,
						channelPriorDPLogNorm);

				for (int dimIdx=0; dimIdx<_channelDimCount; dimIdx++) {
					
					classChannelDimIndices[2] = dimIdx;
					
					final int classChannelDimDataIdx = _classChannelDimData.shape
							.calcFlatIndexFromLocation(classChannelDimLoc);

					_classChannelDimData.set(
							classChannelDimDataIdx + CLASS_CHANNEL_DIM_PRIOR_DP_ALPHA_PROB, 
							channelDimPriorDPAlphaProbs[dimIdx]);
				}
			}
		}

		_temperature = Double.NaN;
	}
	
	public double getTemperature() {
		return _temperature;
	}
	
	public void setTemperature(final double temperature) {
		if (temperature <= 0.0 || temperature >= 1.0) {
			throw new IllegalArgumentException(
					"Temperature must be within interval " + 
					"(0.0, 1.0), but got " + temperature);
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
		final double classPriorDPLogNorm = 
				_classData.get(CLASS_PRIOR_DP_LOG_NORM);
		final double classWouldbeSamplesSum =
				_classData.get(CLASS_ADDED_SAMPLES_SUM) + 1.0;
		
		// compute class assignment log-likelihoods
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			// log-likelihood population index
			final int classProbsIdx = classProbsStart + classIdx;
			
			// init log-likelihood calculation
			// with class DP normalization constant
			classProbs[classProbsIdx] = classPriorDPLogNorm;

			// calculate log-likelihood under the DP prior of assigning to this class
			for (int classIdx2=0; classIdx2<_classCount; classIdx2++) {

				// would-be class index
				classDimIndices[0] = classIdx2;
				
				final int classDimDataIdx = _classDimData.shape
						.calcFlatIndexFromLocation(classDimLoc);
	
				// get prior class info
				final double classDimPriorDPAlphaProb = 
						_classDimData.get(classDimDataIdx + CLASS_DIM_PRIOR_DP_ALPHA_PROB);
				
				// would-be the number of samples of this class
				final double classDimWouldbeSamples =
						_classDimData.get(classDimDataIdx + CLASS_DIM_ADDED_SAMPLES)
						+ (classIdx == classIdx2 ? 1.0 : 0.0);
				
				// would-be sample probability of this class
				final double classSampleProb =
						classDimWouldbeSamples /
						classWouldbeSamplesSum;
				
				// apply simulated annealing
				final double annealedClassProb =
						_temperature / _classCount +
						(1.0 - _temperature) * classSampleProb;
				
				// add DP log-likelihood
				classProbs[classProbsIdx] += 
						(classDimPriorDPAlphaProb - 1.0) * 
						Math.log(annealedClassProb);
			}

			classDimIndices[0] = classIdx;
			classChannelIndices[0] = classIdx;
			classChannelDimIndices[0] = classIdx;

			// channels distribution log-likelihood
			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				classChannelIndices[1] = channelIdx;
				classChannelDimIndices[1] = channelIdx;
				
				final int classChannelDataIdx = _classChannelData.shape
						.calcFlatIndexFromLocation(classChannelLoc);

				// meta channel information
				final double classChannelPriorDPLogNorm = 
						_classChannelData.get(classChannelDataIdx + 
								CLASS_CHANNEL_PRIOR_DP_LOG_NORM);
				final double classChannelWouldbeSamplesSum =
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
					final double classChannelDimPriorDPAlphaProb = 
							_classChannelDimData.get(classChannelDimDataIdx + 
									CLASS_CHANNEL_DIM_PRIOR_DP_ALPHA_PROB);
					
					// would-be the number of samples of this dim
					final double classChannelDimWouldbeSamples = 
							_classChannelDimData.get(classChannelDimDataIdx + 
									CLASS_CHANNEL_DIM_ADDED_SAMPLES) + channelDimValue;
					
					// would-be sample probability of this dim
					final double classChannelDimSampleProb =
							classChannelDimWouldbeSamples /
							classChannelWouldbeSamplesSum;
					
					// apply simulated annealing
					final double annealedClassChannelDimProb =
							_temperature / _channelDimCount +
							(1.0 - _temperature) * classChannelDimSampleProb;
					
					// add log-likelihood
					classProbs[classProbsIdx] += 
							(classChannelDimPriorDPAlphaProb - 1.0) * 
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
									CLASS_CHANNEL_DIM_PRIOR_DP_ALPHA_PROB);
					
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
