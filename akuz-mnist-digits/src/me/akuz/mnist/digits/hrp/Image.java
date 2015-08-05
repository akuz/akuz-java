package me.akuz.mnist.digits.hrp;

/**
 * Image being analyzed.
 */
public final class Image {
	
	private final double _size;
	private final ImagePatch _root;
	
	public Image(final int size) {
		_size = size;
		_root = new ImagePatch(size, null, null);
	}

}
