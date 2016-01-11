package me.akuz.mnist.digits.visor;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;

/**
 * Color-inferring visor layer, which collapses
 * the infinite number of potential multi-channel
 * colors into a finite set of colors that can
 * the visor can recognize.
 *
 */
public final class VisorLayerC extends VisorLayer {

	private Tensor _input;
	
	private Tensor _recorded;
	
	private Tensor _patterns;
	
	/**
	 * Number of colors recognized by the layer.
	 */
	public final int colorCount;
	
	/**
	 * Output to be used at the next level.
	 */
	public final Tensor output;
	
	/**
	 * Create color visor layer with the shape of the
	 * input tensor (must have ndim 3 with the last
	 * dimension spanning the color channels) and 
	 * a specified number of colors to recognize.
	 */
	public VisorLayerC(
			final Shape inputShape, 
			final int colorCount) {
		
		super(inputShape);
		
		if (inputShape.ndim != 3) {
			throw new IllegalArgumentException(
				"Shape of the color input must have ndim 3");
		}
		
		if (colorCount < 2) {
			throw new IllegalArgumentException(
				"Color count must be >= 2, got " + colorCount);
		}
		
		this.colorCount = colorCount;
		
		final Shape outputShape = new Shape(
				inputShape.sizes[0],
				inputShape.sizes[1],
				colorCount);
		
		this.output = new DenseTensor(outputShape);
		
		// Shape dataShape = new Shape(colorCount, channelCount, 2)
	}
	
	public void setInput(final Tensor input) {
		if (input != null) {
			if (this.inputShape.equals(input.shape)) {
				throw new IllegalArgumentException(
					"Input shape " + input.shape + " doesn't match " +
					"layer input shape " + this.inputShape);
			}
		}
		_input = input;
	}

	@Override
	public void infer() {
		// TODO Auto-generated method stub
		if (_input != null) {
			
		}
	}

	@Override
	public void record() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void optimize() {
		// TODO Auto-generated method stub
		
	}

}
