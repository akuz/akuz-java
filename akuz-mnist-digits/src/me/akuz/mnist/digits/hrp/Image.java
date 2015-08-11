package me.akuz.mnist.digits.hrp;

import me.akuz.core.geom.ByteImage;

/**
 * Specific image, which is being analyzed.
 */
public final class Image {
	
	private final ByteImage _byteImage;
	private final double _averageIntensity;
	
	public Image(final ByteImage byteImage) {
		_byteImage = byteImage;
		double intensity = 0.0;
		for (int i=0; i<byteImage.getRowCount(); i++) {
			for (int j=0; j<byteImage.getColCount(); j++) {
				intensity += byteImage.getIntensity(i, j);
			}
		}
		_averageIntensity = intensity 
				/ byteImage.getRowCount() 
				/ byteImage.getColCount();
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
	
	public double getIntensity(
			final double centerX,
			final double centerY,
			final double size) {
		
		// TODO
		return _averageIntensity;
	}
}
