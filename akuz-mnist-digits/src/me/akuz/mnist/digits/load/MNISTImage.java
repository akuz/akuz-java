package me.akuz.mnist.digits.load;

import me.akuz.core.geom.ByteImage;

public final class MNISTImage {
	
	private final int _digit;
	private final ByteImage _byteImage;
	
	public MNISTImage(int digit, ByteImage byteImage) {
		if (digit < 0 || digit > 9) {
			throw new IllegalArgumentException(
					"Digit must be within [0,9], got " + digit);
		}
		_digit = digit;
		_byteImage = byteImage;
	}
	
	public int getDigit() {
		return _digit;
	}
	
	public ByteImage getByteImage() {
		return _byteImage;
	}

}
