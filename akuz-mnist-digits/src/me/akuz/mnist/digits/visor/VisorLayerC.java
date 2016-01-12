package me.akuz.mnist.digits.visor;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.StringUtils;
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
	
	public static final double PATTERN_ALPHA = 1.0;
	public static final double PATTERN_CHANNEL_ALPHA = 1.0;
	public static final double PATTERN_CHANNEL_ALPHA_NOISE = 0.1;

	private Tensor _input;
	private Tensor _patterns;
	private Tensor _patternCounts;

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

		// patterns: prior
		Tensor patternsPrior = new DenseTensor(
				new Shape(
						this.outputColorCount, 
						this.inputChannelCount,
						2));
		
		// patterns: prior init
		for (int idx=0; idx<patternsPrior.size; idx++) {
			patternsPrior.set(idx, 
					PATTERN_CHANNEL_ALPHA + 
					rnd.nextDouble()*PATTERN_CHANNEL_ALPHA_NOISE);
		}
		
		// patterns: posterior
		_patterns = new AddTensor(patternsPrior);

		// pattern probs: prior
		Tensor patternCountsPrior = new DenseTensor(
				new Shape(this.outputColorCount));
		
		// pattern probs: prior init
		for (int idx=0; idx<patternCountsPrior.size; idx++) {
			patternCountsPrior.set(idx, PATTERN_ALPHA);
		}
		
		// pattern counts: posterior
		_patternCounts = new AddTensor(patternCountsPrior);
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
		final int[] inputIndices = new int[3];
		final int[] outputIndices = new int[3];
		final int[] patternIndices = new int[3];
		final Location inputLocation = new Location(inputIndices);
		final Location outputLocation = new Location(outputIndices);
		final Location patternLocation = new Location(patternIndices);
		final double[] colors = new double[this.outputColorCount];
		for (int i=0; i<this.inputHeight;i++) {
			inputIndices[0] = i;
			outputIndices[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIndices[1] = j;
				outputIndices[1] = j;
				
				// FIXME: add "dreamed" prior from above
				
				// reset colors array
				Arrays.fill(colors, 0.0);
				
				// calculate color log probs
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// prior observation probability 
					colors[colorIdx] += StatsUtils.checkFinite(
							Math.log(_patternCounts.get(colorIdx)));
					
					// set first pattern index
					patternIndices[0] = colorIdx;
					
					// now add channel observations probability
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						inputIndices[2] = channelIdx;
						patternIndices[1] = channelIdx;
						
						final double value1 = input.get(inputLocation);
						final double value0 = 1.0 - value1;
						
						patternIndices[2] = 0;
						final double alpha0 = _patterns.get(patternLocation);
						
						patternIndices[2] = 1;
						final double alpha1 = _patterns.get(patternLocation);
						
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
					outputIndices[2] = colorIdx;
					this.output.set(outputLocation, colors[colorIdx]);
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
		final int[] inputIndices = new int[3];
		final int[] outputIndices = new int[3];
		final int[] patternIndices = new int[3];
		final Location inputLocation = new Location(inputIndices);
		final Location outputLocation = new Location(outputIndices);
		final Location patternLocation = new Location(patternIndices);
		final double[] channels = new double[this.inputChannelCount];
		for (int i=0; i<this.inputHeight;i++) {
			inputIndices[0] = i;
			outputIndices[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIndices[1] = j;
				outputIndices[1] = j;
				
				// reset channels array
				Arrays.fill(channels, 0.0);
				
				// aggregate color probs
				for (int colorIdx=0; colorIdx<this.outputColorCount; colorIdx++) {
					
					// get output color prob
					outputIndices[2] = colorIdx;
					final double colorProb = this.output.get(outputLocation);
					
					// aggregate pattern channels
					patternIndices[0] = colorIdx;
					
					// now add channel observations probability
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						patternIndices[1] = channelIdx;
						
						patternIndices[2] = 0;
						final double alpha0 = _patterns.get(patternLocation);
						
						patternIndices[2] = 1;
						final double alpha1 = _patterns.get(patternLocation);
						
						// calculate probability of 1
						final double value1 = alpha1 / (alpha0 + alpha1);
						
						// merge into the channel
						channels[channelIdx] += colorProb * value1;
					}
				}
				
				// set input channel values at this location
				for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
					
					// populate channel value
					inputIndices[2] = channelIdx;
					input.set(inputLocation, channels[channelIdx]);
				}
			}
		}
	}

	@Override
	public void learn() {
		
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

					// add to pattern observations
					_patternCounts.add(colorIdx, colorProb);

					// update pattern channels
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
	
	public void print() {
		System.out.println("------ pattern probs ------");
		System.out.println(_patternCounts.toString());
		System.out.println("------ patterns ------");
		System.out.println(_patterns.toString());
	}

}
