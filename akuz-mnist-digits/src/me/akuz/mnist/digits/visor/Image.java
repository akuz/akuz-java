package me.akuz.mnist.digits.visor;

/**
 * Image of a fixed height, width, and a number
 * of color channels (which is called depth).
 *
 */
public final class Image {
	
	private final int _height;
	private final int _width;
	private final int _depth;
	private final double[][][] _tensor;
	
	public Image(int height, int width, int depth) {
		if (height <= 0) {
			throw new IllegalArgumentException("Image height must be positive, got " + height);
		}
		if (width <= 0) {
			throw new IllegalArgumentException("Image width must be positive, got " + width);
		}
		if (depth <= 0) {
			throw new IllegalArgumentException("Image depth must be positive, got " + depth);
		}
		_height = height;
		_width = width;
		_depth = depth;
		_tensor = new double[height][width][depth];
	}
	
	public int getHeight() {
		return _height;
	}
	
	public int getWidth() {
		return _width;
	}
	
	public int getDepth() {
		return _depth;
	}
	
	public double[][][] getTensor() {
		return _tensor;
	}

}
