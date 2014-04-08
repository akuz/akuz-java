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

public final class InferHDP {
	
	private static final double HIER_DIR_ALPHA = 1.0;
	private static final double PARENT_DIST_ALPHA = 0.1;
	private static final double PARENT_DIST_ALPHA_INIT = 0.1;
	private static final double LOG_INSURANCE = 0.000000000000000000000001;
	
	private final List<FeatureImage> _images;
	private final int _inputDim;
	private int _parentFeatureShift;
	private DirDist[][] _parentFeatureBlocks;
	private List<FeatureImage> _parentFeatureImages;
	private final int _featureDim;
	private final int _featureShift;
	private final List<FeatureImage> _featureImages;
	private DirDist[][] _featureBlocks;
	private double[] _featureProbs;
	
	public InferHDP(
			final List<FeatureImage> images,
			final int inputDim, 
			final int featureDim,
			final int featureShift) {
		
		if (inputDim < 2) {
			throw new IllegalArgumentException("Input feature dimensionality must be >= 2");
		}
		if (featureDim < 2) {
			throw new IllegalArgumentException("Feature dimensionality must be >= 2");
		}
		if (featureShift < 1) {
			throw new IllegalArgumentException("Feature shift must be positive");
		}

		_images = images;
		_inputDim = inputDim;
		_featureDim = featureDim;
		_featureShift = featureShift;
		
		_featureImages = new ArrayList<>();
		for (int i=0; i<images.size(); i++) {
			FeatureImage image = images.get(i);
			FeatureImage featureImage = new FeatureImage(image.getRowCount()-featureShift, image.getColCount()-featureShift, featureDim);
			_featureImages.add(featureImage);
		}
	}

	public void setParentFeatureImages(int parentFeatureShift, DirDist[][] parentFeatureBlocks, List<FeatureImage> parentFeatureImages) {
		if (parentFeatureShift < 1) {
			throw new IllegalArgumentException("Feature shift must be positive");
		}
		if (_images.size() != parentFeatureImages.size()) {
			throw new IllegalArgumentException("Numbers of images and parent feature images do not match");
		}
		if (parentFeatureBlocks.length < 2) {
			throw new IllegalArgumentException("Parent feature dim must be >= 2");
		}
		for (int f2=0; f2<parentFeatureBlocks.length; f2++) {
			if (parentFeatureBlocks[f2].length != 4) {
				throw new IllegalArgumentException("Parent feature blocks are not 2x2");
			}
			for (int l=0; l<parentFeatureBlocks[f2].length; l++) {
				if (parentFeatureBlocks[f2][l].getDim() != _featureDim) {
					throw new IllegalArgumentException("Parent feature blocks have incompatible child dimensionality");
				}
			}
		}
		_parentFeatureShift = parentFeatureShift;
		_parentFeatureBlocks = parentFeatureBlocks;
		_parentFeatureImages = parentFeatureImages;
	}
	
	public void execute(
			final Monitor parentMonitor,
			final int maxIterationCount,
			final double logLikeChangeThreshold) {
		
		if (maxIterationCount <= 0) {
			throw new IllegalArgumentException("Max iteration count must be positive");
		}
		if (logLikeChangeThreshold <= 0) {
			throw new IllegalArgumentException("LogLike change threshold must be positive");
		}

		final Monitor monitor = parentMonitor == null ? null : new LocalMonitor(this.getClass().getSimpleName() + " (shift " + _featureShift + ")", parentMonitor);
		DecimalFormat fmt = new DecimalFormat("' '0.00000000;'-'0.00000000");

		final Random rnd = ThreadLocalRandom.current();
		
		double[] currProbs;
//		if (_featureProbs != null) {
//			currProbs = _featureProbs;
//		} else {
			currProbs = new double[_featureDim];
			Arrays.fill(currProbs, 1.0 / _featureDim);
//		}
		
		double[] nextProbs = new double[_featureDim];
		Arrays.fill(nextProbs, 0);
		
		DirDist[][] currBlocks;
//		if (_featureBlocks != null) {
//			currBlocks = _featureBlocks;
//		} else {
			currBlocks = new DirDist[_featureDim][4];
			for (int k=0; k<_featureDim; k++) {
				for (int l=0; l<4; l++) {
					currBlocks[k][l] = new DirDist(_inputDim, PARENT_DIST_ALPHA);
					for (int d=0; d<_inputDim; d++) {
						currBlocks[k][l].addObservation(d, PARENT_DIST_ALPHA_INIT * rnd.nextDouble());
					}
					currBlocks[k][l].normalize();
				}
			}
//		}
		DirDist[][] nextBlocks = new DirDist[_featureDim][4];
		for (int k=0; k<_featureDim; k++) {
			for (int l=0; l<4; l++) {
				nextBlocks[k][l] = new DirDist(_inputDim, PARENT_DIST_ALPHA);
			}
		}
		
		int iter = 0;
		double prevLogLike = Double.NaN;
		double[] logLikes = new double[_featureDim];
		
		if (monitor != null) {
			monitor.write("Total " + _images.size() + " images");
			monitor.write("Starting with:");
			for (int k=0; k<_featureDim; k++) {
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
			
			for (int imageIndex=0; imageIndex<_images.size(); imageIndex++) {
				
				if (monitor != null) {
					if ((imageIndex+1) % 100 == 0) {
						monitor.write((imageIndex+1) + " images processed");
					}
				}
				
				final FeatureImage image = _images.get(imageIndex);
				final FeatureImage featureImage = _featureImages.get(imageIndex);
				final FeatureImage parentFeatureImage = _parentFeatureImages != null ? _parentFeatureImages.get(imageIndex) : null;
				
				for (int row=0; row<image.getRowCount()-_featureShift; row++) {
					for (int col=0; col<image.getColCount()-_featureShift; col++) {


						// init log likes
						if (parentFeatureImage != null) {
							
							Arrays.fill(logLikes, 0);
							
							// top-left of parent
							{
								final int parentIdx = 0;
								final int parentRow = row;
								final int parentCol = col;
								if (parentRow >= 0 && 
									parentCol >= 0 &&
									parentRow < parentFeatureImage.getRowCount() && 
									parentCol < parentFeatureImage.getColCount()) {
									
									double[] parentBlockFeatureProbs = parentFeatureImage.getFeatureProbs(parentRow, parentCol);
									for (int f2=0; f2<parentBlockFeatureProbs.length; f2++) {
										
										double parentProb = parentBlockFeatureProbs[f2];
										double[] parentPosterior = _parentFeatureBlocks[f2][parentIdx].getPosterior();
										
										for (int f=0; f<_featureDim; f++) {
											logLikes[f] += parentProb * parentPosterior[f];
										}
									}
								}
							}

							// top-right of parent
							{
								final int parentIdx = 1;
								final int parentRow = row;
								final int parentCol = col - _parentFeatureShift;
								if (parentRow >= 0 && 
									parentCol >= 0 &&
									parentRow < parentFeatureImage.getRowCount() && 
									parentCol < parentFeatureImage.getColCount()) {
									
									double[] parentBlockFeatureProbs = parentFeatureImage.getFeatureProbs(parentRow, parentCol);
									for (int f2=0; f2<parentBlockFeatureProbs.length; f2++) {
										
										double parentProb = parentBlockFeatureProbs[f2];
										double[] parentPosterior = _parentFeatureBlocks[f2][parentIdx].getPosterior();
										
										for (int f=0; f<_featureDim; f++) {
											logLikes[f] += parentProb * parentPosterior[f];
										}
									}
								}
							}

							// bottom-left of parent
							{
								final int parentIdx = 2;
								final int parentRow = row - _parentFeatureShift;
								final int parentCol = col;
								if (parentRow >= 0 && 
									parentCol >= 0 &&
									parentRow < parentFeatureImage.getRowCount() && 
									parentCol < parentFeatureImage.getColCount()) {
									
									double[] parentBlockFeatureProbs = parentFeatureImage.getFeatureProbs(parentRow, parentCol);
									for (int f2=0; f2<parentBlockFeatureProbs.length; f2++) {
										
										double parentProb = parentBlockFeatureProbs[f2];
										double[] parentPosterior = _parentFeatureBlocks[f2][parentIdx].getPosterior();
										
										for (int f=0; f<_featureDim; f++) {
											logLikes[f] += parentProb * parentPosterior[f];
										}
									}
								}
							}

							// bottom-right of parent
							{
								final int parentIdx = 3;
								final int parentRow = row - _parentFeatureShift;
								final int parentCol = col - _parentFeatureShift;
								if (parentRow >= 0 && 
									parentCol >= 0 &&
									parentRow < parentFeatureImage.getRowCount() && 
									parentCol < parentFeatureImage.getColCount()) {
									
									double[] parentBlockFeatureProbs = parentFeatureImage.getFeatureProbs(parentRow, parentCol);
									for (int f2=0; f2<parentBlockFeatureProbs.length; f2++) {
										
										double parentProb = parentBlockFeatureProbs[f2];
										double[] parentPosterior = _parentFeatureBlocks[f2][parentIdx].getPosterior();
										
										for (int f=0; f<_featureDim; f++) {
											logLikes[f] += parentProb * parentPosterior[f];
										}
									}
								}
							}
							
							// log parent induced priors
							for (int f=0; f<_featureDim; f++) {
								logLikes[f] = Math.log(logLikes[f]);
							}
							
						} else {
							
							// log frequency based priors
							for (int k=0; k<_featureDim; k++) {
								logLikes[k] = Math.log(currProbs[k]);
							}
						}
												
						// add data log likes
						for (int k=0; k<_featureDim; k++) {
							{
								final double[] parentProbs = currBlocks[k][0].getPosterior();
								final double[] probs = image.getFeatureProbs(row, col);
								for (int d=0; d<probs.length; d++) {
									logLikes[k] += (parentProbs[d]*HIER_DIR_ALPHA - 1) * Math.log(LOG_INSURANCE + probs[d]);
								}
							}
							{
								final double[] parentProbs = currBlocks[k][1].getPosterior();
								final double[] probs = image.getFeatureProbs(row, col+_featureShift);
								for (int d=0; d<probs.length; d++) {
									logLikes[k] += (parentProbs[d]*HIER_DIR_ALPHA - 1) * Math.log(LOG_INSURANCE + probs[d]);
								}
							}
							{
								final double[] parentProbs = currBlocks[k][2].getPosterior();
								final double[] probs = image.getFeatureProbs(row+_featureShift, col);
								for (int d=0; d<probs.length; d++) {
									logLikes[k] += (parentProbs[d]*HIER_DIR_ALPHA - 1) * Math.log(LOG_INSURANCE + probs[d]);
								}
							}
							{
								final double[] parentProbs = currBlocks[k][3].getPosterior();
								final double[] probs = image.getFeatureProbs(row+_featureShift, col+_featureShift);
								for (int d=0; d<probs.length; d++) {
									logLikes[k] += (parentProbs[d]*HIER_DIR_ALPHA - 1) * Math.log(LOG_INSURANCE + probs[d]);
								}
							}
						}
						
						// add to current log likelihood
						currLogLike += StatsUtils.logSumExp(logLikes);

						// normalize probabilities of features
						StatsUtils.logLikesToProbsReplace(logLikes);
						
						// save feature probs to feature image
						double[] featureProbs = featureImage.getFeatureProbs(row, col);
						for (int f=0; f<_featureDim; f++) {
							featureProbs[f] = logLikes[f];
						}

						// add to next probs
						for (int k=0; k<_featureDim; k++) {
							nextProbs[k] += logLikes[k];
						}
						for (int k=0; k<_featureDim; k++) {
							if (logLikes[k] > 0) {
								{
									double[] probs = image.getFeatureProbs(row, col);
									nextBlocks[k][0].addObservation(probs, logLikes[k]);
								}
								{
									double[] probs = image.getFeatureProbs(row, col+_featureShift);
									nextBlocks[k][1].addObservation(probs, logLikes[k]);
								}
								{
									double[] probs = image.getFeatureProbs(row+_featureShift, col);
									nextBlocks[k][2].addObservation(probs, logLikes[k]);
								}
								{
									double[] probs = image.getFeatureProbs(row+_featureShift, col+_featureShift);
									nextBlocks[k][3].addObservation(probs, logLikes[k]);
								}
							}
						}
					}
				}
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
				for (int k=0; k<_featureDim; k++) {
					for (int l=0; l<4; l++) {
						nextBlocks[k][l] = new DirDist(_inputDim, PARENT_DIST_ALPHA);
					}
				}
			}

			if (monitor != null) {
				for (int k=0; k<_featureDim; k++) {
					monitor.write("Latent state #" + (k+1));
					monitor.write("  Prob: " + fmt.format(currProbs[k]));
					for (int l=0; l<4; l++) {
						monitor.write("  Block #" + (l+1) + ": " + currBlocks[k][l]);
					}
				}
				monitor.write("LogLike: " + currLogLike + " (" + prevLogLike + ")");
			}
			
			// check log like error
			if (Double.isNaN(currLogLike)) {
				if (monitor != null) {
					monitor.write("Log likelihood error.");
				}
				break;
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
		
		_featureProbs = currProbs;
		_featureBlocks = currBlocks;
	}
	
	public int getFeatureDim() {
		return _featureDim;
	}
	
	public int getFeatureShift() {
		return _featureShift;
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
