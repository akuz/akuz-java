package me.akuz.mnist.digits.visor;

/**
 * Visor looks at an image and performs
 * the inference of its hidden states.
 *
 */
public final class Visor {

	private boolean _configured;
	private final int _height;
	private final int _width;
	private final int _depth;
	private Image _image;

	public Visor(int width, int height, int depth) {
		if (height <= 0) {
			throw new IllegalArgumentException("Visor height must be positive, got " + height);
		}
		if (width <= 0) {
			throw new IllegalArgumentException("Visor width must be positive, got " + width);
		}
		if (depth <= 0) {
			throw new IllegalArgumentException("Visor depth must be positive, got " + depth);
		}
		_height = height;
		_width = width;
		_depth = depth;
	}
	
	public void configure() {
		if (_configured) {
			throw new IllegalStateException("Already configured");
		}
		
		// TODO: perform configuration
		
		_configured = true;
	}

	public void setImage(final Image image) {
		if (image.getHeight() != _height) {
			throw new IllegalArgumentException(
					"Image must be of height " + _height + 
					", got " + image.getHeight());
		}
		if (image.getWidth() != _width) {
			throw new IllegalArgumentException(
					"Image must be of width " + _width + 
					", got " + image.getWidth());
		}
		if (image.getDepth() != _depth) {
			throw new IllegalArgumentException(
					"Image must be of depth " + _depth + 
					", got " + image.getDepth());
		}
		_image = image;
	}
	
	public void infer() {
		Image image = _image;
		if (image != null) {
			
			// TODO: infer values of hidden states
		}
	}
	

}
