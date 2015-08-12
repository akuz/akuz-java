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
	
	public double getIntensity(
			final double centerX,
			final double centerY,
			final double size) {
		
		final double halfSize = size / 2.0;
		final double startX = centerX - halfSize;
		final double startY = centerY - halfSize;
		final double endX = centerX + halfSize;
		final double endY = centerY + halfSize;
		
		final int nrow = _byteImage.getRowCount();
		final int ncol = _byteImage.getColCount();
		
		int counter = 0;
		double intensitySum = 0.0;
		
		for (double x = startX; x <= endX; x += 1.0) {
			for (double y = startY; y <= endY; y += 1.0) {
				
				int i = (int)x;
				int j = (int)y;
				
				if (i >= 0 && i < nrow && j >=0 && j < ncol) {
					
					counter++;
					intensitySum += _byteImage.getIntensity(i, j);
				}
			}
		}

		return counter > 0 ? intensitySum / counter : 0.0;
	}
}
