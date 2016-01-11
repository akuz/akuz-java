package me.akuz.mnist.digits.visor;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Location;
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
	private Tensor _patterns;

	/**
	 * Height of the input image tensor (size of dim 0).
	 */
	public final int inputHeight;

	/**
	 * Width of the input image tensor (size of dim 1).
	 */
	public final int inputWidth;
	
	/**
	 * Number of channels in the input image (size of dim 2).
	 */
	public final int inputChannelCount;
	
	/**
	 * Number of colors recognized by the layer.
	 */
	public final int outputColorCount;
	
	/**
	 * Shape of the output tensor.
	 */
	public final Shape outputShape;
	
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
		
		this.inputHeight = inputShape.sizes[0];
		this.inputWidth = inputShape.sizes[1];
		this.inputChannelCount = inputShape.sizes[2];
		
		if (this.inputHeight < 1) {
			throw new IllegalArgumentException(
				"Input image tensor height (size of dim 0) must be positive");
		}
		if (this.inputWidth < 1) {
			throw new IllegalArgumentException(
				"Input image tensor width (size of dim 1) must be positive");
		}
		if (this.inputChannelCount < 1) {
			throw new IllegalArgumentException(
				"Input channel count (size of dim 2) must be positive");
		}
		
		if (colorCount < 2) {
			throw new IllegalArgumentException(
				"Color count must be >= 2, got " + colorCount);
		}
		this.outputColorCount = colorCount;
		
		this.outputShape = new Shape(
				this.inputHeight,
				this.inputWidth,
				colorCount);
		
		this.output = new DenseTensor(outputShape);

		// 2D Dirichlet over each color and channel
		final Shape patternsShape = new Shape(
				this.outputColorCount, 
				this.inputChannelCount,
				2);
		
		_patterns = new DenseTensor(patternsShape);
		
		// TODO: initialize patterns with prior noise
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
		
		int[] inputIndices = new int[3];
		int[] outputIndices = new int[3];
		int[] patternIndices = new int[3];
		Location inputLoc = new Location(inputIndices);
		Location outputLoc = new Location(outputIndices);
		Location patternLoc = new Location(patternIndices);
		
		double[] channelValues = new double[this.inputChannelCount];

		for (int i=0; i<this.inputHeight; i++) {
			inputIndices[0] = i;
			outputIndices[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIndices[1] = j;
				outputIndices[1] = j;

				// collect channel values
				for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
					
					inputIndices[2] = channelIdx;
					
					final double channelValue = this._input.get(inputLoc);
					if (channelValue < 0.0 || channelValue > 1.0) {
						throw new IllegalStateException(
							"Input image can only contain values " +
							"within interval [0, 1], got " + channelValue);
					}
					
					channelValues[channelIdx] = channelValue;
				}
				
				// update patterns for each color
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// get output color prob
					outputIndices[2] = colorIdx;
					final double colorProb = this.output.get(outputLoc);

					// update color pattern
					patternIndices[0] = colorIdx;
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						patternIndices[1] = channelIdx;

						// get channel value at this point
						final double channelValue = channelValues[channelIdx];

						// update dim 0
						patternIndices[2] = 0;
						_patterns.add(patternLoc, colorProb*(1.0 - channelValue));
						
						// update dim 1
						patternIndices[2] = 1;
						_patterns.add(patternLoc, colorProb*channelValue);
					}
				}
			}
		}
	}

}
