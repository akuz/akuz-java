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

	public static final int ROOT_CLASS_PRIOR_DP_ALPHA = 0;
	public static final int ROOT_CLASS_PRIOR_DP_LOG_NORM = 1;
	public static final int ROOT_CLASS_ADDED_SAMPLES_SUM = 2;
	public static final int LENGTH_ROOT = 3;

	public static final int CLASS_PRIOR_DB_PROB = 0;
	public static final int CLASS_ADDED_SAMPLES = 1;
	public static final int LENGTH_CLASS = 2;

	public static final int CHANNEL_PRIOR_MEAN = 0;
	public static final int CHANNEL_PRIOR_MEAN_SAMPLES = 1;
	public static final int CHANNEL_PRIOR_SIGMA = 2;
	public static final int CHANNEL_PRIOR_SIGMA_SAMPLES = 3;
	public static final int CHANNEL_ADDED_SUM = 4;
	public static final int CHANNEL_ADDED_SUM_SQ = 5;
	public static final int CHANNEL_ADDED_SAMPLES = 6;
	public static final int LENGTH_CHANNEL = 7;

	public static final int UNDERLYING_PRIOR_DP_PROB = 0;
	public static final int UNDERLYING_ADDED_SAMPLES = 1;
	public static final int LENGTH_UNDERLYING = 2;

	private final int _classCount;
	private final int _channelCount;
	private final int _underlyingCount;

	private final Tensor _rootData;
	private final Tensor _classData;
	private final Tensor _channelData;
	private final Tensor _underlyingData;

	private double _temperature;
	
	public DPClasses(
			final int classCount,
			final int channelCount,
			final int underlyingCount) {
		
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
		
		if (underlyingCount < 2) {
			throw new IllegalArgumentException(
					"underlyingCount must be >= 2, got " + 
					underlyingCount);
		}
		_underlyingCount = underlyingCount;
		
		_rootData = new Tensor(new Shape(LENGTH_ROOT));
		_classData = new Tensor(new Shape(_classCount, LENGTH_CLASS));
		_channelData = new Tensor(new Shape(_classCount, _channelCount, LENGTH_CHANNEL));
		_underlyingData = new Tensor(new Shape(_classCount, _channelCount, _underlyingCount, LENGTH_UNDERLYING));
		
		// init class and channel priors
		final Random rnd = ThreadLocalRandom.current();
		final int[] classIndices = new int[_channelData.ndim];
		final Location classLoc = new Location(classIndices);
		final int[] channelIndices = new int[_channelData.ndim];
		final Location channelLoc = new Location(channelIndices);
		final int[] underlyingIndices = new int[_underlyingData.ndim];
		final Location underlyingLoc = new Location(underlyingIndices);

		// TODO from arguments
		_classPriorDPAlpha = 100.0 * _classCount;
		
		// init class prior DP prob
		final double[] classPriorDPProbs = new double[_classCount];
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			// TODO from arguments
			final double priorClassObs = 1.0 + rnd.nextDouble()*0.01;
			classPriorDPProbs[classIdx] = priorClassObs;
		}
		StatsUtils.normalizeInPlace(classPriorDPProbs);
		
		double priorClassDPLogNorm = 0.0;
		double priorClassDPAlphaProbSum = 0.0;
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			classIndices[0] = classIdx;
			channelIndices[0] = classIdx;
			underlyingIndices[0] = classIdx;
			
			final double classPriorDPProb = classPriorDPProbs[classIdx];
			final double priorClassDPAlphaProb = _classPriorDPAlpha * classPriorDPProb;
			
			priorClassDPLogNorm -= GammaFunction.lnGamma(priorClassDPAlphaProb);
			priorClassDPAlphaProbSum += priorClassDPAlphaProb;

			final int classDataIdx = _classData.shape.calcFlatIndexFromLocation(classLoc);

			_classData.set(
					classDataIdx + CLASS_PRIOR_DB_PROB, 
					classPriorDPProbs[classIdx]);

			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				channelIndices[1] = channelIdx;
				underlyingIndices[1] = channelIdx;
				
				final int channelDataIdx = _channelData.shape
						.calcFlatIndexFromLocation(channelLoc);

				// TODO from arguments
				final double priorMean = 0.5 + rnd.nextDouble()*0.01;
				final double priorMeanSamples = 10.0;
				_channelData.set(
						channelDataIdx + CHANNEL_PRIOR_MEAN, 
						priorMean);
				_channelData.set(
						channelDataIdx + CHANNEL_PRIOR_MEAN_SAMPLES, 
						priorMeanSamples);

				// TODO from arguments
				final double priorSigma = 1.0 + rnd.nextDouble()*0.01;
				final double priorSigmaSamples = 10.0;
				_channelData.set(
						channelDataIdx + CHANNEL_PRIOR_SIGMA, 
						priorSigma);
				_channelData.set(
						channelDataIdx + CHANNEL_PRIOR_SIGMA_SAMPLES,
						priorSigmaSamples);
				
				// TODO: init underlying
			}
		}
		priorClassDPLogNorm += GammaFunction.lnGamma(priorClassDPAlphaProbSum);
		_classPriorDPLogNorm = priorClassDPLogNorm;

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
		
		final int[] classIndices = new int[_channelData.ndim];
		final Location classLoc = new Location(classIndices);
		
		final int[] channelIndices = new int[_channelData.ndim];
		final Location channelLoc = new Location(channelIndices);
		
		// compute class assignment log-likelihoods
		for (int classIdx=0; classIdx<_classCount; classIdx++) {

			classIndices[0] = classIdx;
			channelIndices[0] = classIdx;

			final int classDataIdx = _classData.shape.calcFlatIndexFromLocation(classLoc);

			// get prior class info
			final double priorClassDPProb = _classData.get(classDataIdx + CLASS_PRIOR_DB_PROB);
			final double priorClassDPAlphaProb = _classPriorDPAlpha * priorClassDPProb;
			
			// compute absolute index
			final int classProbsIdx = classProbsStart + classIdx;
			
			// init log-likelihood calculation
			classProbs[classProbsIdx] = 0.0;
			
			// add class DP normalization constant
			classProbs[classProbsIdx] += _classPriorDPLogNorm;
			
			// would-be total number of class samples
			final double wouldbeClassSamplesSum =
					_classAddedSamplesSum + 1.0;
			
			// class distribution log-likelihood
			for (int newClassIdx=0; newClassIdx<_classCount; newClassIdx++) {
				
				// would-be the number of samples of this class
				final double wouldbeClassSamples = 
						_classData.get(classDataIdx + CLASS_ADDED_SAMPLES) +
						(classIdx == newClassIdx ? 1.0 : 0.0);
				
				// would-be sample probability of this class
				final double wouldbeClassProb =
						wouldbeClassSamples /
						wouldbeClassSamplesSum;
				
				// apply simulated annealing
				final double annealedClassProb =
						_temperature * priorClassDPProb +
						(1.0 - _temperature) * wouldbeClassProb;
				
				// add log-likelihood
				classProbs[classProbsIdx] += 
						(priorClassDPAlphaProb - 1.0) * 
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
		
		final int[] classIndices = new int[_channelData.ndim];
		final Location classLoc = new Location(classIndices);
		
		final int[] channelIndices = new int[_channelData.ndim];
		final Location channelLoc = new Location(channelIndices);
		
		for (int classIdx=0; classIdx<_classCount; classIdx++) {
			
			classIndices[0] = classIdx;
			channelIndices[0] = classIdx;
			
			final int classDataIdx = _classData.shape.calcFlatIndexFromLocation(classLoc);

			// get provided class probability
			final double classProb = classProbs[classProbsStart + classIdx];
			
			// class stats
			_classData.add(
					classDataIdx + CLASS_ADDED_SAMPLES, 
					multiplier*classProb);
			_classAddedSamplesSum += 
					multiplier*classProb;
			
			for (int channelIdx=0; channelIdx<_channelCount; channelIdx++) {
				
				channelIndices[1] = channelIdx;
				
				final int channelDataIdx = _channelData.shape.calcFlatIndexFromLocation(channelLoc);

				// get provided channel value
				final double channelValue = channelData[channelDataStart + channelIdx];
				
				// channel stats
				_channelData.add(
						channelDataIdx + CHANNEL_ADDED_SUM, 
						multiplier*classProb*channelValue);
				_channelData.add(
						channelDataIdx + CHANNEL_ADDED_SUM_SQ, 
						multiplier*classProb*channelValue*channelValue);
				_channelData.add(
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
		
		final int[] channelIndices = new int[_channelData.ndim];
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
				
				final int channelDataIdx = _channelData.shape.calcFlatIndexFromLocation(channelLoc);
				
				final double priorMean = _channelData.get(channelDataIdx + CHANNEL_PRIOR_MEAN);
				final double priorMeanSamples = _channelData.get(channelDataIdx + CHANNEL_PRIOR_MEAN_SAMPLES);

				final double addedSum = _channelData.get(channelDataIdx + CHANNEL_ADDED_SUM);
				final double addedSamples = _channelData.get(channelDataIdx + CHANNEL_ADDED_SAMPLES);
				
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
