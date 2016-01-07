package me.akuz.mnist.digits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.akuz.core.DecimalFmt;
import me.akuz.core.geom.BWImage;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.math.NIGDist;
import me.akuz.core.math.NIGDistUtils;
import me.akuz.core.math.StatsUtils;

/**
 * Infers pixel categories from pixel intensities.
 *
 */
public final class InferCAT {
	
	private static final int PRIOR_SAMPLES = 1000;
	
	private final List<BWImage> _pixelImages;
	private final List<ProbImage> _featureImages;

//	private List<ProbImage> _parentFeatureImages;
//	private List<ProbImage> _parentOpacityImages;
	
	private final int _featureDim;
	private double[] _featureProbs;
	private NIGDist[] _featureDists;
	
	public InferCAT(
			final List<BWImage> images,
			final int featureDim) {
		
		if (featureDim < 2 || featureDim > 255) {
			throw new IllegalArgumentException("Argument featureDim must within interval [2,255]");
		}

		_pixelImages = images;

		_featureDim = featureDim;
		
		_featureImages = new ArrayList<>(images.size());
		for (int i=0; i<images.size(); i++) {
			final BWImage pixelImage = images.get(i);
			final ProbImage featureImage = new ProbImage(pixelImage.getRowCount(), pixelImage.getColCount(), featureDim);
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
		
		double[] currFeatureProbs = new double[_featureDim];
		Arrays.fill(currFeatureProbs, 1.0 / _featureDim);
		
		double[] nextFeatureProbs = new double[_featureDim];
		Arrays.fill(nextFeatureProbs, 0);
		
		NIGDist[] currFeatureDists = NIGDistUtils.createPriorsOnIntervalEvenly(_featureDim, 0.0, 1.0, PRIOR_SAMPLES);
		NIGDist[] nextFeatureDists = NIGDistUtils.createPriorsOnIntervalEvenly(_featureDim, 0.0, 1.0, PRIOR_SAMPLES);
		
		int iter = 0;
		double prevLogLike = Double.NaN;
		double[] logLikes = new double[_featureDim];
		
		if (monitor != null) {
			monitor.write("Total " + _pixelImages.size() + " images");
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
			
			for (int imageIndex=0; imageIndex<_pixelImages.size(); imageIndex++) {
				
				if (monitor != null) {
					if ((imageIndex+1) % 100 == 0) {
						monitor.write((imageIndex+1) + " images processed");
					}
				}
				
				final BWImage pixelImage = _pixelImages.get(imageIndex);
				final ProbImage featureImage = _featureImages.get(imageIndex);
				
				for (int row=0; row<pixelImage.getRowCount(); row++) {
					for (int col=0; col<pixelImage.getColCount(); col++) {
						
						// calc pixel feature logLikes
						for (int k=0; k<_featureDim; k++) {
							logLikes[k] 
									= Math.log(currFeatureProbs[k]) 
									+ Math.log(currFeatureDists[k].getProb(pixelImage.getColor(row, col)));
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
								nextFeatureDists[k].addObservation(pixelImage.getColor(row, col), featureProbs[k]);
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
				currFeatureDists = nextFeatureDists;
				nextFeatureDists = NIGDistUtils.createPriorsOnIntervalEvenly(_featureDim, 0.0, 1.0, PRIOR_SAMPLES);
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
