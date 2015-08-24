package me.akuz.mnist.digits.hrp;

import me.akuz.core.geom.BWImage;
import me.akuz.mnist.digits.load.MNISTImage;

/**
 * Specific image, which is being analyzed.
 */
public final class Image {
	
	private final int _digit;
	private final BWImage _bwImage;
	
	public Image(final int digit, final BWImage bwImage) {
		_digit = digit;
		_bwImage = bwImage;
	}
	
	public Image(final MNISTImage mnistImage) {
		_digit = mnistImage.getDigit();
		_bwImage = new BWImage(mnistImage.getByteImage());
	}
	
	public int getDigit() {
		return _digit;
	}
	
	public BWImage getBWImage() {
		return _bwImage;
	}
	
	public int getMinSize() {
		return Math.min(
				_bwImage.getRowCount(),
				_bwImage.getColCount());
	}
	
	public double getCenterX() {
		return (double)_bwImage.getRowCount() / 2.0;
	}
	
	public double getCenterY() {
		return (double)_bwImage.getColCount() / 2.0;
	}
	
	public double getColor(
			final double centerX,
			final double centerY,
			final double size) {
		
		final double halfSize = size / 2.0;
		final double startX = centerX - halfSize + 0.5;
		final double startY = centerY - halfSize + 0.5;
		final double endX = centerX + halfSize;
		final double endY = centerY + halfSize;
		
		final int nrow = _bwImage.getRowCount();
		final int ncol = _bwImage.getColCount();
		
		int counter = 0;
		double intensitySum = 0.0;
		
		for (double x = startX; x < endX; x += 1.0) {
			for (double y = startY; y < endY; y += 1.0) {
				
				int i = (int)x;
				int j = (int)y;
				
				if (i >= 0 && i < nrow && j >=0 && j < ncol) {
					
					counter++;
					intensitySum += _bwImage.getColor(i, j);
				}
			}
		}
		
		if (counter == 0) {
			throw new InternalError("This should not happen");
		}

		return counter > 0 ? intensitySum / counter : 0.0;
	}
	
	public void addColor(
			final double centerX,
			final double centerY,
			final double size,
			final double intensity) {
		
		final double halfSize = size / 2.0;
		final double startX = centerX - halfSize + 0.5;
		final double startY = centerY - halfSize + 0.5;
		final double endX = centerX + halfSize;
		final double endY = centerY + halfSize;
		
		final int nrow = _bwImage.getRowCount();
		final int ncol = _bwImage.getColCount();
		
		for (double x = startX; x < endX; x += 1.0) {
			for (double y = startY; y < endY; y += 1.0) {
				
				int i = (int)x;
				int j = (int)y;
				
				if (i >= 0 && i < nrow && j >=0 && j < ncol) {
					
					_bwImage.addColor(i, j, intensity);
				}
			}
		}
	}	
}
