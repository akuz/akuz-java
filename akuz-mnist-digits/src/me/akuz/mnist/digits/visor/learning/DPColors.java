package me.akuz.mnist.digits.visor.learning;

import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;
import me.akuz.mnist.digits.visor.VisorLayer;
import me.akuz.mnist.digits.visor.algo.DPClasses;

/**
 * Color-inferring visor layer, which collapses
 * the infinite number of potential multi-channel
 * colors into a finite set of colors that can
 * the visor can recognize.
 *
 */
public final class DPColors extends VisorLayer {

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
	public final int inputChannels;
	
	/**
	 * Number of dims in channels in the input image (size of dim 3).
	 */
	public final int inputChannelDim;
	
	/**
	 * Number of colors recognized by the layer.
	 */
	public final int colorCount;
	
	/**
	 * Shape of the output tensor.
	 */
	public final Shape outputShape;
	
	/**
	 * Output to be used at the next level.
	 */
	public final Tensor output;

	private final DPClasses _colors;

	/**
	 * Create color visor layer with the shape of the
	 * input tensor (must have ndim 3 with the last
	 * dimension spanning the color channels) and 
	 * a specified number of colors to recognize.
	 */
	public DPColors(
			final Shape inputShape, 
			final int colorCount,
			final double startTemperature) {
		
		super(inputShape);
		
		if (inputShape.ndim != 4) {
			throw new IllegalArgumentException(
				"Shape of the color input must have ndim 4");
		}
		
		this.inputHeight = inputShape.sizes[0];
		this.inputWidth = inputShape.sizes[1];
		this.inputChannels = inputShape.sizes[2];
		this.inputChannelDim = inputShape.sizes[3];
		
		if (this.inputHeight < 1) {
			throw new IllegalArgumentException(
				"Input image tensor height (size of dim 0) must be positive");
		}
		if (this.inputWidth < 1) {
			throw new IllegalArgumentException(
				"Input image tensor width (size of dim 1) must be positive");
		}
		if (this.inputChannels < 1) {
			throw new IllegalArgumentException(
				"inputChannels (size of dim 2) must be positive");
		}
		if (this.inputChannelDim < 2) {
			throw new IllegalArgumentException(
				"inputChannelDim (size of dim 3) must be >= 2");
		}
		
		if (colorCount < 2) {
			throw new IllegalArgumentException(
				"Color count must be >= 2, got " + colorCount);
		}
		this.colorCount = colorCount;
		
		this.outputShape = new Shape(
				this.inputHeight,
				this.inputWidth,
				colorCount);
		
		this.output = new Tensor(outputShape);

		_colors = new DPClasses(
				colorCount,
				inputChannels,
				inputChannelDim);
		
		_colors.setTemperature(startTemperature);
	}
	
	public void setTemperature(final double temperature) {
		_colors.setTemperature(temperature);
	}
	
	private void iterate() {
		
		final Tensor input = getInputNotNull();

		final int[] inputIndices = new int[4];
		final Location inputLoc = new Location(inputIndices);
		
		final int[] outputIndices = new int[3];
		final Location outputLoc = new Location(outputIndices);
				
		final int[] channelDataStarts = new int[this.inputChannels];
		
		for (int rowIdx=0; rowIdx<this.inputHeight; rowIdx++) {
			
			inputIndices[0] = rowIdx;
			outputIndices[0] = rowIdx;
			
			for (int columnIdx=0; columnIdx<this.inputWidth; columnIdx++) {
				
				inputIndices[1] = columnIdx;
				outputIndices[1] = columnIdx;
				
				for (int channelIdx=0; channelIdx<this.inputChannels; channelIdx++) {
					inputIndices[2] = channelIdx;
					channelDataStarts[channelIdx] = inputShape.calcFlatIndexFromLocation(inputLoc);
				}
				
				final int outputDataStart = outputShape.calcFlatIndexFromLocation(outputLoc);

				_colors.iterate(
						output.data(), 
						outputDataStart, 
						input.data(), 
						channelDataStarts);
			}
		}
	}

	@Override
	public void infer() {
		iterate();
	}

	@Override
	public void learn() {
		iterate();
	}

	@Override
	public void dream() {

		final Tensor input = getInputNotNull();

		final int[] inputIndices = new int[4];
		final Location inputLoc = new Location(inputIndices);
		
		final int[] outputIndices = new int[3];
		final Location outputLoc = new Location(outputIndices);
				
		final int[] channelDataStarts = new int[this.inputChannels];
		
		for (int rowIdx=0; rowIdx<this.inputHeight; rowIdx++) {
			
			inputIndices[0] = rowIdx;
			outputIndices[0] = rowIdx;
			
			for (int columnIdx=0; columnIdx<this.inputWidth; columnIdx++) {
				
				inputIndices[1] = columnIdx;
				outputIndices[1] = columnIdx;
				
				for (int channelIdx=0; channelIdx<this.inputChannels; channelIdx++) {
					inputIndices[2] = channelIdx;
					channelDataStarts[channelIdx] = inputShape.calcFlatIndexFromLocation(inputLoc);
				}
				
				final int outputDataStart = this.outputShape.calcFlatIndexFromLocation(outputLoc);
				
				_colors.calculateChannelMeans(
						output.data(), 
						outputDataStart, 
						input.data(), 
						channelDataStarts);
			}
		}
	}

}
