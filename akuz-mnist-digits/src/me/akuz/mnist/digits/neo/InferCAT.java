package me.akuz.mnist.digits.neo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.DecimalFmt;
import me.akuz.core.geom.ByteImage;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.math.NIGDist;
import me.akuz.core.math.StatsUtils;
import me.akuz.mnist.digits.ProbImage;

/**
 * Infers pixel categories from pixel intensities.
 *
 */
public final class InferCAT {
	
	private static final double PRIOR_MEAN = 0.5;
	private static final double PRIOR_MEAN_SAMPLES = 0.1;
	private static final double PRIOR_VAR = Math.pow(0.3, 2);
	private static final double PRIOR_VAR_SAMPLES = 0.1;
	private static final double INIT_OBSERVATION_WEIGHT = 0.1;
	
	private final List<ByteImage> _baseImages;
	
	private final int _featureDim;
	private final List<ProbImage> _featureImages;

	private double[] _featureProbs;
	private NIGDist[] _featureDists;
	
	public InferCAT(
			final List<ByteImage> baseImages,
			final int featureDim) {
		
		if (featureDim < 2 || featureDim > 255) {
			throw new IllegalArgumentException("Argument featureDim must within interval [2,255]");
		}

		_baseImages = baseImages;
		_featureDim = featureDim;
		
		_featureImages = new ArrayList<>(baseImages.size());
		for (int i=0; i<baseImages.size(); i++) {
			final ByteImage baseImage = baseImages.get(i);
			final ProbImage featureImage = new ProbImage(baseImage.getRowCount(), baseImage.getColCount(), featureDim);
			_featureImages.add(featureImage);
		}
	}
	
	public final void execute(
			final Monitor parentMonitor, 
			final int maxIterationCount, 
			final double logLikeChangeThreshold) {

		if (maxIterationCount <= 0) {
			throw new IllegalArgumentException("Max iteration count must be positive");
		}
		if (logLikeChangeThreshold <= 0) {
			throw new IllegalArgumentException("LogLike change threshold must be positive");
		}

		final Monitor monitor = parentMonitor == null ? null : new LocalMonitor(this.getClass().getSimpleName(), parentMonitor);

		final Random rnd = ThreadLocalRandom.current();
		
		double[] currFeatureProbs = new double[_featureDim];
		Arrays.fill(currFeatureProbs, 1.0 / _featureDim);
		
		double[] nextFeatureProbs = new double[_featureDim];
		Arrays.fill(nextFeatureProbs, 0);
		
		NIGDist[] currFeatureDists = new NIGDist[_featureDim];
		for (int k=0; k<_featureDim; k++) {
			currFeatureDists[k] = new NIGDist(PRIOR_MEAN, PRIOR_MEAN_SAMPLES, PRIOR_VAR, PRIOR_VAR_SAMPLES);
			currFeatureDists[k].addObservation(rnd.nextDouble(), INIT_OBSERVATION_WEIGHT);
		}
		
		NIGDist[] nextFeatureDists = new NIGDist[_featureDim];
		for (int k=0; k<_featureDim; k++) {
			nextFeatureDists[k] = new NIGDist(PRIOR_MEAN, PRIOR_MEAN_SAMPLES, PRIOR_VAR, PRIOR_VAR_SAMPLES);
		}
		
		int iter = 0;
		double prevLogLike = Double.NaN;
		double[] logLikes = new double[_featureDim];
		
		if (monitor != null) {
			monitor.write("Total " + _baseImages.size() + " images");
			monitor.write("Starting with:");
			for (int k=0; k<_featureDim; k++) {
				monitor.write("Feature #" + (k+1));
				monitor.write("  Prob: " + DecimalFmt.formatZeroSpacePlus8D(currFeatureProbs[k]));
				monitor.write("  Dist: " + currFeatureDists[k]);
			}
		}
		
		while (true) {

			iter += 1;
			
			if (monitor != null) {
				monitor.write("Iteration " + iter);
			}
			
			double currLogLike = 0;
			
			for (int imageIndex=0; imageIndex<_baseImages.size(); imageIndex++) {
				
				if (monitor != null) {
					if ((imageIndex+1) % 100 == 0) {
						monitor.write((imageIndex+1) + " images processed");
					}
				}
				
				final ByteImage image = _baseImages.get(imageIndex);
				final ProbImage featureImage = _featureImages.get(imageIndex);
				
				for (int row=0; row<image.getRowCount(); row++) {
					for (int col=0; col<image.getColCount(); col++) {

						// calc pixel feature logLikes
						for (int k=0; k<_featureDim; k++) {
							logLikes[k] 
									= Math.log(currFeatureProbs[k]) 
									+ Math.log(currFeatureDists[k].getProb(image.getIntensity(row, col)));
						}
						
						// add to current log likelihood
						currLogLike += StatsUtils.logSumExp(logLikes);

						// normalize probabilities of features
						StatsUtils.logLikesToProbsReplace(logLikes);
						
						// save feature probs to feature image
						final double[] featureProbs = featureImage.getFeatureProbs(row, col);
						for (int k=0; k<_featureDim; k++) {
							featureProbs[k] = logLikes[k];
						}

						// add to next feature probs
						for (int k=0; k<_featureDim; k++) {
							nextFeatureProbs[k] += featureProbs[k];
						}
						for (int k=0; k<_featureDim; k++) {
							if (featureProbs[k] > 0) {
								nextFeatureDists[k].addObservation(image.getIntensity(row, col), featureProbs[k]);
							}
						}
					}
				}
			}
			
			// normalize next probs
			StatsUtils.normalize(nextFeatureProbs);
			
			// update current probs
			{
				double[] tmp = currFeatureProbs;
				currFeatureProbs = nextFeatureProbs;
				nextFeatureProbs = tmp;
				Arrays.fill(nextFeatureProbs, 0);
			}
			{
				NIGDist[] tmp = currFeatureDists;
				currFeatureDists = nextFeatureDists;
				nextFeatureDists = tmp;
				for (int k=0; k<_featureDim; k++) {
					nextFeatureDists[k] = new NIGDist(PRIOR_MEAN, PRIOR_MEAN_SAMPLES, PRIOR_VAR, PRIOR_VAR_SAMPLES);
				}
			}

			if (monitor != null) {
				for (int k=0; k<_featureDim; k++) {
					monitor.write("Feature #" + (k+1));
					monitor.write("  Prob: " + DecimalFmt.formatZeroSpacePlus8D(currFeatureProbs[k]));
					monitor.write("  Dist: " + currFeatureDists[k]);
				}
				monitor.write("LogLike: " + currLogLike + " (" + prevLogLike + ")");
			}

			// check log like error
			if (Double.isNaN(currLogLike)) {
				if (monitor != null) {
					monitor.write("Log likelihood is NAN");
				}
				throw new IllegalStateException("Log likelihood is NAN.");
			}
			
			// check log like
			if (currLogLike < prevLogLike) {
				if (monitor != null) {
					monitor.write("Log likelihood fell, but we don't stop");
				}
				
			} else {
			
				// check if converged
				if (Double.isNaN(prevLogLike) == false &&
					Math.abs(prevLogLike - currLogLike) < logLikeChangeThreshold) {
					if (monitor != null) {
						monitor.write("Log likelihood converged.");
					}
					break;
				}
			}

			// check if max iterations
			if (iter >= maxIterationCount) {
				if (monitor != null) {
					monitor.write("Done max iterations (" + iter + ").");
				}
				break;
			}
			
			prevLogLike = currLogLike;
		}
		
		_featureProbs = currFeatureProbs;
		_featureDists = currFeatureDists;
	}
	
	public int getFeatureDim() {
		return _featureDim;
	}
	
	public double[] getFeatureProbs() {
		return _featureProbs;
	}
	
	public NIGDist[] getFeatureDists() {
		return _featureDists;
	}
	
	public List<ProbImage> getFeatureImages() {
		return _featureImages;
	}

}
