package me.akuz.mnist.digits.visor;

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
	 * Base constructor.
	 */
	public VisorLayer(final Shape inputShape) {
		if (inputShape == null) {
			throw new NullPointerException("inputShape");
		}
		this.inputShape = inputShape;
	}

	/**
	 * Infer the probabilities of the hidden variables, 
	 * based on the current states of the neighboring
	 * variables.
	 */
	public abstract void infer();

	/**
	 * Record the inferred probabilities of hidden
	 * states in the memory for future recognition.
	 */
	public abstract void record();

}
