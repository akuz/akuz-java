package me.akuz.mnist.digits;

/**
 * Infers feature block locations, given an image, 
 * block count, and a set of features.
 *
 */
public final class InferFeatureBlocks {
	
	private final int _imageSize;
	private final int _blockCount;
	private final Feature[] _features;
	
	private final double[][][] _pixelBlockProbs;
	private final double[] _blockProbs;
	private final double[][] _blockFeatureProbs;
	private final int[] _blockX;
	private final int[] _blockY;
	
	public InferFeatureBlocks(int imageSize, int blockCount, Feature[] features) {

		_imageSize = imageSize;
		_blockCount = blockCount;
		_features = features;
		
		_pixelBlockProbs = new double[imageSize][imageSize][blockCount];
		_blockProbs = new double[blockCount];
		_blockFeatureProbs = new double[blockCount][features.length];
		_blockX = new int[blockCount];
		_blockY = new int[blockCount];
	}
	
	public void infer(ByteImage image) {
		
		if (image.getRowCount() != _imageSize) {
			throw new IllegalArgumentException("Image size must be " + _imageSize + "x" + _imageSize +" (got image of height " + image.getRowCount() + ")");
		}
		if (image.getColCount() != _imageSize) {
			throw new IllegalArgumentException("Image size must be " + _imageSize + "x" + _imageSize +" (got image of width " + image.getColCount() + ")");
		}
		
		// TODO: Initialize
		
		// TODO: EM
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
	
	public int[] getBlockX() {
		return _blockX;
	}
	
	public int[] getBlockY() {
		return _blockY;
	}

}
