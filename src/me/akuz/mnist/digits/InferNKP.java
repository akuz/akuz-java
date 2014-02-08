package me.akuz.mnist.digits;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.math.NKPDist;
import me.akuz.core.math.StatsUtils;

public final class InferNKP {
	
	private static final double PRIOR_MEAN = 0.5;
	private static final double PRIOR_MEAN_PRECISION = Math.pow(0.5, -2);
	private static final double PRECISION = Math.pow(0.1, -2);
	private static final double INIT_WEIGHT = 0.1;
	
	private final int _dim;
	private final double[] _featureProbs;
	private final NKPDist[][] _featureBlocks;
	private final List<FeatureImage> _featureImages;
	
	public InferNKP(
			final Monitor parentMonitor, 
			final List<ByteImage> images, 
			final int inputShift,
			final int dim,
			final int maxIterationCount,
			final double logLikeChangeThreshold) {
		
		if (inputShift < 1) {
			throw new IllegalArgumentException("Input shift must be positive");
		}
		if (dim < 2) {
			throw new IllegalArgumentException("Feature dimensionality must be >= 2");
		}
		if (maxIterationCount <= 0) {
			throw new IllegalArgumentException("Max iteration count must be positive");
		}
		if (logLikeChangeThreshold <= 0) {
			throw new IllegalArgumentException("LogLike change threshold must be positive");
		}

		final Monitor monitor = parentMonitor == null ? null : new LocalMonitor(this.getClass().getSimpleName() + " (shift " + inputShift + ")", parentMonitor);
		DecimalFormat fmt = new DecimalFormat("' '0.00000000;'-'0.00000000");

		_dim = dim;
		final Random rnd = ThreadLocalRandom.current();
		
		_featureImages = new ArrayList<>();
		for (int i=0; i<images.size(); i++) {
			ByteImage image = images.get(i);
			FeatureImage featureImage = new FeatureImage(image.getRowCount()-inputShift, image.getColCount()-inputShift, dim);
			_featureImages.add(featureImage);
		}
		
		double[] currProbs = new double[_dim];
		Arrays.fill(currProbs, 1.0 / _dim);
		
		double[] nextProbs = new double[_dim];
		Arrays.fill(nextProbs, 0);
		
		NKPDist[][] currBlocks = new NKPDist[_dim][4];
		for (int k=0; k<_dim; k++) {
			for (int l=0; l<4; l++) {
				currBlocks[k][l] = new NKPDist(PRIOR_MEAN, PRIOR_MEAN_PRECISION, PRECISION);
				currBlocks[k][l].addObservation(rnd.nextDouble(), INIT_WEIGHT);
			}
		}
		
		NKPDist[][] nextBlocks = new NKPDist[_dim][4];
		for (int k=0; k<_dim; k++) {
			for (int l=0; l<4; l++) {
				nextBlocks[k][l] = new NKPDist(PRIOR_MEAN, PRIOR_MEAN_PRECISION, PRECISION);
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
				
				final ByteImage image = images.get(imageIndex);
				final FeatureImage featureImage = _featureImages.get(imageIndex);
				
				byte[][] data = image.getData();
				for (int row=0; row<image.getRowCount()-inputShift; row++) {
					for (int col=0; col<image.getColCount()-inputShift; col++) {

						// init log likes
						for (int k=0; k<_dim; k++) {
							logLikes[k] = Math.log(currProbs[k]);
						}
						
						// add data log likes
						for (int k=0; k<_dim; k++) {
							{
								final int intValue = (int)(data[row][col] & 0xFF);
								final double value = intValue / 255.0;
								logLikes[k] += currBlocks[k][0].getLogProb(value);
							}
							{
								final int intValue = (int)(data[row][col+inputShift] & 0xFF);
								final double value = intValue / 255.0;
								logLikes[k] += currBlocks[k][1].getLogProb(value);
							}
							{
								final int intValue = (int)(data[row+inputShift][col] & 0xFF);
								final double value = intValue / 255.0;
								logLikes[k] += currBlocks[k][2].getLogProb(value);
							}
							{
								final int intValue = (int)(data[row+inputShift][col+inputShift] & 0xFF);
								final double value = intValue / 255.0;
								logLikes[k] += currBlocks[k][3].getLogProb(value);
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
									final int intValue = (int)(data[row][col] & 0xFF);
									final double value = intValue / 255.0;
									nextBlocks[k][0].addObservation(value, logLikes[k]);
								}
								{
									final int intValue = (int)(data[row][col+inputShift] & 0xFF);
									final double value = intValue / 255.0;
									nextBlocks[k][1].addObservation(value, logLikes[k]);
								}
								{
									final int intValue = (int)(data[row+inputShift][col] & 0xFF);
									final double value = intValue / 255.0;
									nextBlocks[k][2].addObservation(value, logLikes[k]);
								}
								{
									final int intValue = (int)(data[row+inputShift][col+inputShift] & 0xFF);
									final double value = intValue / 255.0;
									nextBlocks[k][3].addObservation(value, logLikes[k]);
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
			
			// update current probs
			{
				double[] tmp = currProbs;
				currProbs = nextProbs;
				nextProbs = tmp;
				Arrays.fill(nextProbs, 0);
			}
			{
				NKPDist[][] tmp = currBlocks;
				currBlocks = nextBlocks;
				nextBlocks = tmp;
				for (int k=0; k<_dim; k++) {
					for (int l=0; l<4; l++) {
						nextBlocks[k][l] = new NKPDist(PRIOR_MEAN, PRIOR_MEAN_PRECISION, PRECISION);
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
	
	public NKPDist[][] getFeatureBlocks() {
		return _featureBlocks;
	}
	
	public List<FeatureImage> getFeatureImages() {
		return _featureImages;
	}

}
