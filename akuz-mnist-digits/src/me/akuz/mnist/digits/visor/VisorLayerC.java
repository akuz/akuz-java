package me.akuz.mnist.digits.visor;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.StatsUtils;
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

	// base distribution a color at each pixel
	public static final double PIXEL_COLOR_DP_BASE_INIT    = 1.0;
	public static final double PIXEL_COLOR_DP_BASE_NOISE   = 0.1;
	public static final double PIXEL_COLOR_DP_BASE_MASS    = 10.0;
	public static final double PIXEL_COLOR_DP_MAX_OBS_MASS = 90.0;

	// base distribution for a channel in each color
	public static final double COLOR_CHANNEL_DP_BASE_INIT    = 1.0;
	public static final double COLOR_CHANNEL_DP_BASE_NOISE   = 0.1;
	public static final double COLOR_CHANNEL_DP_BASE_MASS    = 10.0;
	public static final double COLOR_CHANNEL_DP_MAX_OBS_MASS = 90.0;

	private Tensor _input;
	private final Tensor _color;
	private final DDP _colorChannel;

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

		final Random rnd = ThreadLocalRandom.current();

		// colors: prior
		_color = new DenseTensor(
				new Shape(this.outputColorCount));
		
		// colors: prior init
		for (int idx=0; idx<_color.size; idx++) {
			_color.set(idx, 
					PIXEL_COLOR_DP_BASE_INIT + 
					rnd.nextDouble()*PIXEL_COLOR_DP_BASE_NOISE);
		}
		
		_colorChannel = new DDP(
				new Shape(
						this.outputColorCount, 
						this.inputChannelCount,
						2),
				COLOR_CHANNEL_DP_BASE_INIT,
				COLOR_CHANNEL_DP_BASE_NOISE,
				COLOR_CHANNEL_DP_BASE_MASS,
				COLOR_CHANNEL_DP_MAX_OBS_MASS);
	}
	
	public void setInput(final Tensor input) {
		if (input != null) {
			if (!this.inputShape.equals(input.shape)) {
				throw new IllegalArgumentException(
					"Input shape " + input.shape + " doesn't match " +
					"layer input shape " + this.inputShape);
			}
		}
		_input = input;
	}

	@Override
	public void infer(final boolean useOutputAsBaseDist) {
		
		final Tensor input = _input;
		if (input == null) {
			throw new IllegalStateException(
				"Input image not set, cannot infer colors");
		}
		
		final int[] inputIdxs = new int[3];
		final int[] outputIdxs = new int[3];
		final int[] colorChannelIdxs = new int[2];
		final Location inputLoc = new Location(inputIdxs);
		final Location outputLoc = new Location(outputIdxs);
		final Location colorChannelLoc = new Location(colorChannelIdxs);
		final double[] colors = new double[this.outputColorCount];
		final double[] obsData = new double[2];
		for (int i=0; i<this.inputHeight;i++) {
			inputIdxs[0] = i;
			outputIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIdxs[1] = j;
				outputIdxs[1] = j;
				
				// reset colors array
				Arrays.fill(colors, 0.0);
				
				// calculate color log probs
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// prior observation probability 
					colors[colorIdx] += 
						StatsUtils.checkFinite(
							Math.log(_color.get(colorIdx)));
					
					// set first color index
					colorChannelIdxs[0] = colorIdx;
					
					// now add channel observations probability
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						inputIdxs[2] = channelIdx;
						colorChannelIdxs[1] = channelIdx;
						
						obsData[1] = input.get(inputLoc);
						obsData[0] = 1.0 - obsData[1];

						colors[colorIdx] += 
							StatsUtils.checkFinite(
								_colorChannel.calcPosteriorLogLike(
									colorChannelLoc, 
									obsData, 
									0));
					}
				}
				
				// normalize log probabilities
				StatsUtils.logLikesToProbsReplace(colors);

				// set output color probs at this location
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// populate color prob
					outputIdxs[2] = colorIdx;
					this.output.set(outputLoc, colors[colorIdx]);
				}
			}
		}
	}

	@Override
	public void dream() {
		
		final Tensor input = _input;
		if (input == null) {
			throw new IllegalStateException(
				"Input image not set, cannot dream colors");
		}
		
		final int[] inputIdxs = new int[3];
		final int[] outputIdxs = new int[3];
		final int[] colorChannelIdxs = new int[2];
		final Location inputLoc = new Location(inputIdxs);
		final Location outputLoc = new Location(outputIdxs);
		final Location colorChannelLoc = new Location(colorChannelIdxs);
		final double[] channelValues = new double[this.inputChannelCount];
		final double[] obsData = new double[2];
		for (int i=0; i<this.inputHeight;i++) {
			inputIdxs[0] = i;
			outputIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIdxs[1] = j;
				outputIdxs[1] = j;
				
				// reset channels array
				Arrays.fill(channelValues, 0.0);
				
				// aggregate color probs
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// get output color prob
					outputIdxs[2] = colorIdx;
					final double colorProb = this.output.get(outputLoc);
					
					// aggregate color channels
					colorChannelIdxs[0] = colorIdx;
					
					// now add channel observations probability
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						colorChannelIdxs[1] = channelIdx;
						
						_colorChannel.calcPosteriorMean(
								colorChannelLoc, 
								obsData, 
								0);
						
						// merge into the channel
						channelValues[channelIdx] += 
								colorProb * obsData[1];
					}
				}
				
				// set input channel values at this location
				for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
					
					// populate channel value
					inputIdxs[2] = channelIdx;
					input.set(inputLoc, channelValues[channelIdx]);
				}
			}
		}
	}

	@Override
	public void learn() {
		
		final Tensor input = _input;
		if (input == null) {
			throw new IllegalStateException(
				"Input image not set, cannot learn colors");
		}

		final int[] inputIdxs = new int[3];
		final int[] outputIdxs = new int[3];
		final int[] colorChannelIdxs = new int[2];
		final Location inputLoc = new Location(inputIdxs);
		final Location outputLoc = new Location(outputIdxs);
		final Location colorChannelLoc = new Location(colorChannelIdxs);
		
		final double[] channelValues = new double[this.inputChannelCount];
		final double[] obsData = new double[2];

		for (int i=0; i<this.inputHeight; i++) {
			inputIdxs[0] = i;
			outputIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIdxs[1] = j;
				outputIdxs[1] = j;

				// collect channel values
				for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
					
					inputIdxs[2] = channelIdx;
					
					final double channelValue = input.get(inputLoc);
					if (channelValue < 0.0 || channelValue > 1.0) {
						throw new IllegalStateException(
							"Input image channels can only contain values " +
							"within interval [0, 1], got " + channelValue);
					}
					
					channelValues[channelIdx] = channelValue;
				}
				
				// update each color
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// get output color prob
					outputIdxs[2] = colorIdx;
					final double colorProb = this.output.get(outputLoc);

					// add to color counts
					_color.add(colorIdx, colorProb);

					// update color channels
					colorChannelIdxs[0] = colorIdx;
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						colorChannelIdxs[1] = channelIdx;

						// get channel value at this point
						final double channelValue = channelValues[channelIdx];

						obsData[1] = channelValue;
						obsData[0] = 1.0 - channelValue;
						
						_colorChannel.addObservation(
								colorProb, 
								colorChannelLoc, 
								obsData, 
								0);
					}
				}
			}
		}
	}
	
	public void print() {
		System.out.println("------ color counts ------");
		System.out.println(_color.toString());
//		System.out.println("------ colors ------");
//		System.out.println(_colorChannelCounts.toString());
	}

}
