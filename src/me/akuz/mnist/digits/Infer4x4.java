package me.akuz.mnist.digits;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.math.DirDist;
import me.akuz.core.math.StatsUtils;

public final class Infer4x4 {
	
	private static final double HIER_DIR_ALPHA = 1.0;
	private static final double PARENT_DIST_ALPHA = 0.1;
	private static final double PARENT_DIST_ALPHA_INIT = 0.1;
	
	private final int _dim;
	private final double[] _featureProbs;
	private final DirDist[][] _featureBlocks;
	private final List<FeatureImage> _featureImages;
	
	public Infer4x4(
			final Monitor parentMonitor, 
			final List<FeatureImage> images, 
			final int inputDim,
			final int dim,
			final int maxIterationCount,
			final double logLikeChangeThreshold) {
		
		if (dim < 2) {
			throw new IllegalArgumentException("Feature dimensionality must be >= 2");
		}
		if (maxIterationCount <= 0) {
			throw new IllegalArgumentException("Max iteration count must be positive");
		}
		if (logLikeChangeThreshold <= 0) {
			throw new IllegalArgumentException("LogLike change threshold must be positive");
		}

		final Monitor monitor = parentMonitor == null ? null : new LocalMonitor(this.getClass().getSimpleName(), parentMonitor);
		DecimalFormat fmt = new DecimalFormat("' '0.00000000;'-'0.00000000");

		_dim = dim;
		final Random rnd = ThreadLocalRandom.current();
		
		_featureImages = new ArrayList<>();
		for (int i=0; i<images.size(); i++) {
			FeatureImage byteImage = images.get(i);
			FeatureImage featureImage = new FeatureImage(byteImage.getRowCount()-2, byteImage.getColCount()-2, dim);
			_featureImages.add(featureImage);
		}
		
		double[] currProbs = new double[_dim];
		Arrays.fill(currProbs, 1.0 / _dim);
		
		double[] nextProbs = new double[_dim];
		Arrays.fill(nextProbs, 0);
		
		DirDist[][] currBlocks = new DirDist[_dim][4];
		for (int k=0; k<_dim; k++) {
			for (int l=0; l<4; l++) {
				currBlocks[k][l] = new DirDist(inputDim, PARENT_DIST_ALPHA);
				for (int d=0; d<inputDim; d++) {
					currBlocks[k][l].addObservation(d, PARENT_DIST_ALPHA_INIT * rnd.nextDouble());
				}
				currBlocks[k][l].normalize();
			}
		}
		
		DirDist[][] nextBlocks = new DirDist[_dim][4];
		for (int k=0; k<_dim; k++) {
			for (int l=0; l<4; l++) {
				nextBlocks[k][l] = new DirDist(inputDim, PARENT_DIST_ALPHA);
			}
		}
		
		int iter = 0;
		double prevLogLike = Double.NaN;
		double[] logLikes = new double[_dim];
		
		if (monitor != null) {
			monitor.write("Total " + images.size() + " images");
			monitor.write("Starting with:");
			for (int k=0; k<_dim; k++) {
				monitor.write("Latent state #" + (k+1));
				monitor.write("  Prob: " + fmt.format(currProbs[k]));
				for (int l=0; l<4; l++) {
					monitor.write("  Block #" + (l+1) + ": " + currBlocks[k][l]);
				}
			}
		}
		
		while (true) {

			iter += 1;
			
			if (monitor != null) {
				monitor.write("Iteration " + iter);
			}
			
			double currLogLike = 0;
			
			for (int imageIndex=0; imageIndex<images.size(); imageIndex++) {
				
				if (monitor != null) {
					if (imageIndex % 100 == 0) {
						monitor.write(imageIndex + " digit index");
					}
				}
				
				final FeatureImage image = images.get(imageIndex);
				final FeatureImage featureImage = _featureImages.get(imageIndex);
				
				for (int row=0; row<image.getRowCount()-2; row++) {
					for (int col=0; col<image.getColCount()-2; col++) {

						// init log likes
						for (int k=0; k<_dim; k++) {
							logLikes[k] = Math.log(currProbs[k]);
						}
						
						// add data log likes
						for (int k=0; k<_dim; k++) {
							{
								final double[] parentProbs = currBlocks[k][0].getPosterior();
								final double[] probs = image.getFeatureProbs(row, col);
								for (int d=0; d<probs.length; d++) {
									logLikes[k] += (parentProbs[d]*HIER_DIR_ALPHA - 1) * Math.log(probs[d]);
								}
							}
							{
								final double[] parentProbs = currBlocks[k][1].getPosterior();
								final double[] probs = image.getFeatureProbs(row, col+2);
								for (int d=0; d<probs.length; d++) {
									logLikes[k] += (parentProbs[d]*HIER_DIR_ALPHA - 1) * Math.log(probs[d]);
								}
							}
							{
								final double[] parentProbs = currBlocks[k][2].getPosterior();
								final double[] probs = image.getFeatureProbs(row+2, col);
								for (int d=0; d<probs.length; d++) {
									logLikes[k] += (parentProbs[d]*HIER_DIR_ALPHA - 1) * Math.log(probs[d]);
								}
							}
							{
								final double[] parentProbs = currBlocks[k][3].getPosterior();
								final double[] probs = image.getFeatureProbs(row+2, col+2);
								for (int d=0; d<probs.length; d++) {
									logLikes[k] += (parentProbs[d]*HIER_DIR_ALPHA - 1) * Math.log(probs[d]);
								}
							}
						}
						
						// add to current log likelihood
						currLogLike += StatsUtils.logSumExp(logLikes);

						// normalize probabilities of features
						StatsUtils.logLikesToProbsReplace(logLikes);
						
						// save feature probs to feature image
						double[] featureProbs = featureImage.getFeatureProbs(row, col);
						for (int f=0; f<dim; f++) {
							featureProbs[f] = logLikes[f];
						}

						// add to next probs
						for (int k=0; k<_dim; k++) {
							nextProbs[k] += logLikes[k];
						}
						for (int k=0; k<_dim; k++) {
							if (logLikes[k] > 0) {
								{
									double[] probs = image.getFeatureProbs(row, col);
									nextBlocks[k][0].addObservation(probs, logLikes[k]);
								}
								{
									double[] probs = image.getFeatureProbs(row, col+2);
									nextBlocks[k][1].addObservation(probs, logLikes[k]);
								}
								{
									double[] probs = image.getFeatureProbs(row+2, col);
									nextBlocks[k][2].addObservation(probs, logLikes[k]);
								}
								{
									double[] probs = image.getFeatureProbs(row+2, col+2);
									nextBlocks[k][3].addObservation(probs, logLikes[k]);
								}
							}
						}
					}
				}
			}
			
			if (monitor != null) {
				monitor.write("LogLike: " + currLogLike);
			}
			
			// normalize next probs
			StatsUtils.normalize(nextProbs);
			for (int k=0; k<nextBlocks.length; k++) {
				for (int l=0; l<4; l++) {
					nextBlocks[k][l].normalize();
				}
			}
			
			// update current probs
			{
				double[] tmp = currProbs;
				currProbs = nextProbs;
				nextProbs = tmp;
				Arrays.fill(nextProbs, 0);
			}
			{
				DirDist[][] tmp = currBlocks;
				currBlocks = nextBlocks;
				nextBlocks = tmp;
				for (int k=0; k<_dim; k++) {
					for (int l=0; l<4; l++) {
						nextBlocks[k][l] = new DirDist(inputDim, PARENT_DIST_ALPHA);
					}
				}
			}

			if (monitor != null) {
				for (int k=0; k<_dim; k++) {
					monitor.write("Latent state #" + (k+1));
					monitor.write("  Prob: " + fmt.format(currProbs[k]));
					for (int l=0; l<4; l++) {
						monitor.write("  Block #" + (l+1) + ": " + currBlocks[k][l]);
					}
				}
			}
			
			// check if converged
			if (Double.isNaN(prevLogLike) == false &&
				(currLogLike < prevLogLike ||
				 Math.abs(prevLogLike - currLogLike) < logLikeChangeThreshold)) {
				
				if (monitor != null) {
					monitor.write("Log likelihood converged.");
				}
				break;
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
		
		_featureProbs = currProbs;
		_featureBlocks = currBlocks;
	}
	
	public int getDim() {
		return _dim;
	}
	
	public double[] getFeatureProbs() {
		return _featureProbs;
	}
	
	public DirDist[][] getFeatureBlocks() {
		return _featureBlocks;
	}
	
	public List<FeatureImage> getFeatureImages() {
		return _featureImages;
	}
	

}
