package me.akuz.mnist.digits.hrp;

import me.akuz.core.geom.ByteImage;

/**
 * Specific image, which is being analyzed.
 */
public final class Image {
	
	private final ByteImage _byteImage;
	
	public Image(final ByteImage byteImage) {
		_byteImage = byteImage;
	}
	
	public ByteImage getByteImage() {
		return _byteImage;
	}
	
	public int getMinSize() {
		return Math.min(
				_byteImage.getRowCount(),
				_byteImage.getColCount());
	}
	
	public double getCenterX() {
		return (double)_byteImage.getRowCount() / 2.0;
	}
	
	public double getCenterY() {
		return (double)_byteImage.getColCount() / 2.0;
	}
	
	// TODO: helper methods
}
