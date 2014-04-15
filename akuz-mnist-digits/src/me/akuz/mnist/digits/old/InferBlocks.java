package me.akuz.mnist.digits.old;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.geom.ByteImage;
import me.akuz.core.math.StatsUtils;
import me.akuz.mnist.digits.Feature;

/**
 * Infers feature block locations, given an image, 
 * block count, and a set of features.
 *
 */
public final class InferBlocks {
	
	private final static int    ITER_COUNT = 10;
	private final static double PROB_INIT_BASE = 0.5;
	private final static double PROB_INIT_RAND = 0.1;
	
	private final int _imageSize;
	private final int _blockCount;
	private final Feature[] _features;
	
	private final double[][][] _pixelBlockProbs;
	private final double[] _blockProbs;
	private final double[][] _blockFeatureProbs;
	private final double[] _blockX;
	private final double[] _blockY;
	
	private final double[] _tmpFeatureLogLikes;
	
	public InferBlocks(int imageSize, int blockCount, Feature[] features) {

		_imageSize = imageSize;
		_blockCount = blockCount;
		_features = features;
		
		_pixelBlockProbs = new double[imageSize][imageSize][blockCount];
		_blockProbs = new double[blockCount];
		_blockFeatureProbs = new double[blockCount][features.length];
		_blockX = new double[blockCount];
		_blockY = new double[blockCount];
		
		_tmpFeatureLogLikes = new double[features.length];
	}
	
	public void infer(ByteImage image) {
		
		if (image.getRowCount() != _imageSize) {
			throw new IllegalArgumentException("Image size must be " + _imageSize + "x" + _imageSize +" (got image of height " + image.getRowCount() + ")");
		}
		if (image.getColCount() != _imageSize) {
			throw new IllegalArgumentException("Image size must be " + _imageSize + "x" + _imageSize +" (got image of width " + image.getColCount() + ")");
		}
		
		Random rnd = ThreadLocalRandom.current();
		
		// init pixel block probs randomly
		for (int i=0; i<_imageSize; i++) {
			for (int j=0; j<_imageSize; j++) {
				for (int b=0; b<_blockCount; b++) {
					_pixelBlockProbs[i][j][b] = PROB_INIT_BASE + rnd.nextDouble() * PROB_INIT_RAND;
				}
				StatsUtils.normalize(_pixelBlockProbs[i][j]);
			}
		}
		
		// perform fixed number of EM iterations
		for (int iter=1; iter<=ITER_COUNT; iter++) {
			
			// calculate block positions
			Arrays.fill(_blockProbs, 0);
			Arrays.fill(_blockX, 0);
			Arrays.fill(_blockY, 0);
			for (int i=0; i<_imageSize; i++) {
				for (int j=0; j<_imageSize; j++) {
					for (int b=0; b<_blockCount; b++) {
						_blockProbs[b] += _pixelBlockProbs[i][j][b];
						_blockX[b] += _pixelBlockProbs[i][j][b] * i;
						_blockY[b] += _pixelBlockProbs[i][j][b] * j;
					}
				}
			}
			
			// normalize blocks X and Y
			for (int b=0; b<_blockCount; b++) {
				_blockX[b] = //(int)Math.round(
						_blockX[b] / _blockProbs[b]; //);
				_blockY[b] = //(int)Math.round(
						_blockY[b] / _blockProbs[b]; //);
			}
			
			// normalize block probs
			StatsUtils.normalize(_blockProbs);
			
			// calculate block feature probs
			for (int b=0; b<_blockCount; b++) {
				
				int x = (int)_blockX[b];
				int y = (int)_blockY[b];
				
				Arrays.fill(_blockFeatureProbs[b], 0);
				for (int f=0; f<_features.length; f++) {
					
					Feature feature = _features[f];
					final int iMin = Math.max(0, x - feature.getWidthFromMid());
					final int iMax = Math.min(_imageSize-1, x + feature.getWidthFromMid());
					final int jMin = Math.max(0, y - feature.getWidthFromMid());
					final int jMax = Math.min(_imageSize-1, y + feature.getWidthFromMid());
					
					for (int i=iMin; i<=iMax; i++) {
						for (int j=jMin; j<=jMax; j++) {
							_blockFeatureProbs[b][f] += feature.getLogProb(i - x, j - y, image.getIntensity(i, j));
						}
					}
				}
				
				StatsUtils.logLikesToProbsReplace(_blockFeatureProbs[b]);
			}
			
			// re-estimate pixel block probs
			for (int i=0; i<_imageSize; i++) {
				for (int j=0; j<_imageSize; j++) {
					
					for (int b=0; b<_blockCount; b++) {
						
						_pixelBlockProbs[i][j][b] = Math.log(_blockProbs[b]);
						
						int x = (int)_blockX[b];
						int y = (int)_blockY[b];
						
						for (int f=0; f<_features.length; f++) {
							Feature feature = _features[f];

//							_pixelBlockProbs[i][j][b] += Math.log(_blockFeatureProbs[b][f]);
//							_pixelBlockProbs[i][j][b] += feature.getLogProb(i - x, j - y, image.getIntensity(i, j));
							
							_tmpFeatureLogLikes[f] 
									= Math.log(_blockFeatureProbs[b][f])
									+ feature.getLogProb(i - x, j - y, image.getIntensity(i, j));
						}
						
						_pixelBlockProbs[i][j][b] += StatsUtils.logSumExp(_tmpFeatureLogLikes);
					}
					
					StatsUtils.logLikesToProbsReplace(_pixelBlockProbs[i][j]);
				}
			}
		}
	}
	
	public int getImageSize() {
		return _imageSize;
	}
	
	public int getBlockCount() {
		return _blockCount;
	}
	
	public Feature[] getFeatures() {
		return _features;
	}
	
	public double[][][] getPixelBlockProbs() {
		return _pixelBlockProbs;
	}
	
	public double[] getBlockProbs() {
		return _blockProbs;
	}
	
	public double[][] getBlockFeatureProbs() {
		return _blockFeatureProbs;
	}
	
	public double[] getBlockX() {
		return _blockX;
	}
	
	public double[] getBlockY() {
		return _blockY;
	}

}
