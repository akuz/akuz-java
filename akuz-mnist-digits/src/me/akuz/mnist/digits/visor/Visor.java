package me.akuz.mnist.digits.visor;

import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.TensorBase;

/**
 * Visor looks at an image and performs
 * the inference of its hidden states.
 *
 */
public final class Visor {

	private boolean _configured;
	private final Shape _shape;
	private TensorBase _image;

	public Visor(final Shape shape) {
		if (shape == null) {
			throw new NullPointerException("shape");
		}
		if (shape.ndim != 3) {
			throw new IllegalArgumentException("Visor shape must have ndim 3");
		}
		_shape = shape;
	}
	
	public void configure() {
		if (_configured) {
			throw new IllegalStateException("Already configured");
		}
		
		// TODO: perform configuration
		
		_configured = true;
	}

	public void setImage(final TensorBase image) {
		if (image != null) {
			if (!_shape.equals(image.shape)) {
				throw new IllegalArgumentException(
					"Illegal image shape " + image.shape + 
					" for visor of shape " + _shape);
			}
		}
		_image = image;
	}
	
	public void infer() {
		TensorBase image = _image;
		if (image != null) {
			
			// TODO: infer values of hidden states
		}
	}
	

}
