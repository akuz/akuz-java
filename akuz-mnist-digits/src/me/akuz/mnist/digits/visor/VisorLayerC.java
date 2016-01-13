package me.akuz.mnist.digits.visor;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.GammaFunction;
import me.akuz.core.math.StatsUtils;
import me.akuz.ml.tensors.AddTensor;
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

	// base distribution for colors
	public static final double COLOR_BASE_DIR_ALPHA = 1.0;
	public static final double COLOR_BASE_DIR_NOISE = 0.1;

	// base distribution for channels within colors
	public static final double CHANNEL_BASE_DIR_ALPHA = 1.0;
	public static final double CHANNEL_BASE_DIR_NOISE = 0.1;

	private Tensor _input;
	private Tensor _colors;
	private Tensor _colorCounts;

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
		final Tensor colorsPrior = new DenseTensor(
				new Shape(
						this.outputColorCount, 
						this.inputChannelCount,
						2));
		
		// colors: prior init
		for (int idx=0; idx<colorsPrior.size; idx++) {
			colorsPrior.set(idx, 
					CHANNEL_BASE_DIR_ALPHA + 
					rnd.nextDouble()*CHANNEL_BASE_DIR_NOISE);
		}
		
		// colors: posterior
		_colors = new AddTensor(colorsPrior);

		// color counts: prior
		final Tensor colorCountsPrior = new DenseTensor(
				new Shape(this.outputColorCount));
		
		// color counts: prior init
		for (int idx=0; idx<colorCountsPrior.size; idx++) {
			colorCountsPrior.set(idx, 
					COLOR_BASE_DIR_ALPHA + 
					rnd.nextDouble()*COLOR_BASE_DIR_NOISE);
		}
		
		// color counts: posterior
		_colorCounts = new AddTensor(colorCountsPrior);
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
	public void infer() {
		final Tensor input = _input;
		if (input == null) {
			throw new IllegalStateException(
				"Input image not set, cannot infer colors");
		}
		final int[] inputIdxs = new int[3];
		final int[] outputIdxs = new int[3];
		final int[] colorIdxs = new int[3];
		final Location inputLoc = new Location(inputIdxs);
		final Location outputLoc = new Location(outputIdxs);
		final Location colorLoc = new Location(colorIdxs);
		final double[] colors = new double[this.outputColorCount];
		for (int i=0; i<this.inputHeight;i++) {
			inputIdxs[0] = i;
			outputIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIdxs[1] = j;
				outputIdxs[1] = j;
				
				// FIXME: add "dreamed" prior from above
				
				// reset colors array
				Arrays.fill(colors, 0.0);
				
				// calculate color log probs
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// prior observation probability 
					colors[colorIdx] += StatsUtils.checkFinite(
							Math.log(_colorCounts.get(colorIdx)));
					
					// set first color index
					colorIdxs[0] = colorIdx;
					
					// now add channel observations probability
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						inputIdxs[2] = channelIdx;
						colorIdxs[1] = channelIdx;
						
						final double value1 = input.get(inputLoc);
						final double value0 = 1.0 - value1;
						
						colorIdxs[2] = 0;
						final double alpha0 = _colors.get(colorLoc);
						
						colorIdxs[2] = 1;
						final double alpha1 = _colors.get(colorLoc);
						
						colors[colorIdx] += StatsUtils.checkFinite(
								GammaFunction.lnGamma(alpha0 + alpha1) -
								GammaFunction.lnGamma(alpha0) -
								GammaFunction.lnGamma(alpha1) + 
								(alpha0 - 1.0) * Math.log(value0) + 
								(alpha1 - 1.0) * Math.log(value1));
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
		final int[] colorIdxs = new int[3];
		final Location inputLoc = new Location(inputIdxs);
		final Location outputLoc = new Location(outputIdxs);
		final Location colorLoc = new Location(colorIdxs);
		final double[] channels = new double[this.inputChannelCount];
		for (int i=0; i<this.inputHeight;i++) {
			inputIdxs[0] = i;
			outputIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIdxs[1] = j;
				outputIdxs[1] = j;
				
				// reset channels array
				Arrays.fill(channels, 0.0);
				
				// aggregate color probs
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// get output color prob
					outputIdxs[2] = colorIdx;
					final double colorProb = this.output.get(outputLoc);
					
					// aggregate color channels
					colorIdxs[0] = colorIdx;
					
					// now add channel observations probability
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						colorIdxs[1] = channelIdx;
						
						colorIdxs[2] = 0;
						final double alpha0 = _colors.get(colorLoc);
						
						colorIdxs[2] = 1;
						final double alpha1 = _colors.get(colorLoc);
						
						// calculate probability of 1
						final double value1 = alpha1 / (alpha0 + alpha1);
						
						// merge into the channel
						channels[channelIdx] += colorProb * value1;
					}
				}
				
				// set input channel values at this location
				for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
					
					// populate channel value
					inputIdxs[2] = channelIdx;
					input.set(inputLoc, channels[channelIdx]);
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
		final int[] colorIdxs = new int[3];
		final Location inputLoc = new Location(inputIdxs);
		final Location outputLoc = new Location(outputIdxs);
		final Location colorLoc = new Location(colorIdxs);
		
		final double[] channels = new double[this.inputChannelCount];

		for (int i=0; i<this.inputHeight; i++) {
			inputIdxs[0] = i;
			outputIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIdxs[1] = j;
				outputIdxs[1] = j;

				// collect channel values
				for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
					
					inputIdxs[2] = channelIdx;
					
					final double channel = input.get(inputLoc);
					if (channel < 0.0 || channel > 1.0) {
						throw new IllegalStateException(
							"Input image channels can only contain values " +
							"within interval [0, 1], got " + channel);
					}
					
					channels[channelIdx] = channel;
				}
				
				// update each color
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// get output color prob
					outputIdxs[2] = colorIdx;
					final double colorProb = this.output.get(outputLoc);

					// add to color counts
					_colorCounts.add(colorIdx, colorProb);

					// update color channels
					colorIdxs[0] = colorIdx;
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						colorIdxs[1] = channelIdx;

						// get channel value at this point
						final double channel = channels[channelIdx];

						// update dim 0
						colorIdxs[2] = 0;
						_colors.add(colorLoc, colorProb*(1.0 - channel));
						
						// update dim 1
						colorIdxs[2] = 1;
						_colors.add(colorLoc, colorProb*channel);
					}
				}
			}
		}
	}
	
	public void print() {
		System.out.println("------ color counts ------");
		System.out.println(_colorCounts.toString());
		System.out.println("------ colors ------");
		System.out.println(_colors.toString());
	}

}
