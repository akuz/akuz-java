package me.akuz.mnist.digits.visor;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Shape;

/**
 * Base class for layers within a visor.
 *
 */
public abstract class VisorLayer {
	
	/**
	 * Shape of the input data.
	 */
	public final Shape inputShape;
	
	/**
	 * Input tensor (might be null).
	 */
	protected DenseTensor _input;
	
	/**
	 * Base constructor.
	 */
	public VisorLayer(final Shape inputShape) {
		if (inputShape == null) {
			throw new NullPointerException("inputShape");
		}
		this.inputShape = inputShape;
	}
	
	/**
	 * Set new input tensor (must be the 
	 * same shape as layer input shape).
	 */
	public final void setInput(final DenseTensor input) {
		if (input == null) {
			throw new NullPointerException("input");
		}
		if (!input.shape.equals(this.inputShape)) {
			throw new IllegalArgumentException(
					"Input tensor shape, got " + input.shape + 
					", must match the tensor input shape " +
					this.inputShape);
		}
		_input = input;
	}
	
	/**
	 * Get current input tensor.
	 */
	public final DenseTensor getInput() {
		return _input;
	}

	/**
	 * Infer the probabilities of the hidden variables, 
	 * based on the current states of the other layers.
	 */
	public abstract void infer(boolean useOutputAsPrior);

	/**
	 * Learn the inferred probabilities of hidden
	 * states in the memory for future recognition.
	 */
	public abstract void learn();

	/**
	 * Dream the values in the lower layer, given the 
	 * current probabilities of the hidden variables.
	 */
	public abstract void dream();

}
