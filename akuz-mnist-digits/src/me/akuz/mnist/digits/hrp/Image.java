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
	
	// TODO: helper methods
}
