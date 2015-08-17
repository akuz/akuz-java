package me.akuz.mnist.digits.hrp;

import me.akuz.core.geom.IntensImage;
import me.akuz.mnist.digits.load.MNISTImage;

/**
 * Specific image, which is being analyzed.
 */
public final class Image {
	
	private final int _digit;
	private final IntensImage _intensImage;
	
	public Image(final int digit, final IntensImage byteImage) {
		_digit = digit;
		_intensImage = byteImage;
	}
	
	public Image(final MNISTImage mnistImage) {
		_digit = mnistImage.getDigit();
		_intensImage = new IntensImage(mnistImage.getByteImage());
	}
	
	public int getDigit() {
		return _digit;
	}
	
	public IntensImage getIntensImage() {
		return _intensImage;
	}
	
	public int getMinSize() {
		return Math.min(
				_intensImage.getRowCount(),
				_intensImage.getColCount());
	}
	
	public double getCenterX() {
		return (double)_intensImage.getRowCount() / 2.0;
	}
	
	public double getCenterY() {
		return (double)_intensImage.getColCount() / 2.0;
	}
	
	public double getIntensity(
			final double centerX,
			final double centerY,
			final double size) {
		
		final double halfSize = size / 2.0;
		final double startX = centerX - halfSize + 0.5;
		final double startY = centerY - halfSize + 0.5;
		final double endX = centerX + halfSize;
		final double endY = centerY + halfSize;
		
		final int nrow = _intensImage.getRowCount();
		final int ncol = _intensImage.getColCount();
		
		int counter = 0;
		double intensitySum = 0.0;
		
		for (double x = startX; x < endX; x += 1.0) {
			for (double y = startY; y < endY; y += 1.0) {
				
				int i = (int)x;
				int j = (int)y;
				
				if (i >= 0 && i < nrow && j >=0 && j < ncol) {
					
					counter++;
					intensitySum += _intensImage.getIntensity(i, j);
				}
			}
		}
		
		if (counter == 0) {
			throw new InternalError("This should not happen");
		}

		return counter > 0 ? intensitySum / counter : 0.0;
	}
	
	public void addIntensity(
			final double centerX,
			final double centerY,
			final double size,
			final double intensity) {
		
		final double halfSize = size / 2.0;
		final double startX = centerX - halfSize + 0.5;
		final double startY = centerY - halfSize + 0.5;
		final double endX = centerX + halfSize;
		final double endY = centerY + halfSize;
		
		final int nrow = _intensImage.getRowCount();
		final int ncol = _intensImage.getColCount();
		
		for (double x = startX; x < endX; x += 1.0) {
			for (double y = startY; y < endY; y += 1.0) {
				
				int i = (int)x;
				int j = (int)y;
				
				if (i >= 0 && i < nrow && j >=0 && j < ncol) {
					
					_intensImage.addIntensity(i, j, intensity);
				}
			}
		}
	}	
}
