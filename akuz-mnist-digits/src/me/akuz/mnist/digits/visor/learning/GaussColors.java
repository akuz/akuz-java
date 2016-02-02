package me.akuz.mnist.digits.visor.learning;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.StatsUtils;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;
import me.akuz.mnist.digits.visor.VisorLayer;
import me.akuz.mnist.digits.visor.algo.GaussClasses;

/**
 * Color-inferring visor layer, which collapses
 * the infinite number of potential multi-channel
 * colors into a finite set of colors that can
 * the visor can recognize.
 *
 */
public final class GaussColors extends VisorLayer {

	private final GaussClasses _colors;

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
	public final int colorCount;
	
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
	public GaussColors(
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
		this.colorCount = colorCount;
		
		this.outputShape = new Shape(
				this.inputHeight,
				this.inputWidth,
				colorCount);
		
		this.output = new Tensor(outputShape);
		
		final Random rnd = ThreadLocalRandom.current();
		final int[] outputIndices = new int[3];
		final Location outputLoc = new Location(outputIndices);
		final double[] outputData = this.output.data();
		for (int i=0; i<this.inputHeight; i++) {
			outputIndices[0] = i;
			for (int j=0; j<this.inputWidth; j++) {
				outputIndices[1] = j;
				final int idx = this.outputShape.calcFlatIndexFromLocation(outputLoc);
				for (int k=0; k<colorCount; k++) {
					outputData[idx + k] = 0.5 + rnd.nextDouble()*0.01;
				}
				StatsUtils.normalizeInPlace(outputData, idx, colorCount);
			}
		}

		_colors = new GaussClasses(
				this.colorCount,
				this.inputChannelCount);
	}

	@Override
	public void infer() {
		
		final Tensor input = getInputNotNull();
		
		final int[] outputDeepIdxs = new int[3];
		final int[] inputDeepIdxs = new int[3];
		final Location outputDeepLoc = new Location(outputDeepIdxs);
		final Location inputDeepLoc = new Location(inputDeepIdxs);
		final double[] outputData = this.output.data();
		final double[] inputData = input.data();
		
		for (int i=0; i<this.inputHeight;i++) {
			outputDeepIdxs[0] = i;
			inputDeepIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				outputDeepIdxs[1] = j;
				inputDeepIdxs[1] = j;
				
				// get flat indices
				final int outputStartIdx = outputShape.calcFlatIndexFromLocation(outputDeepLoc);
				final int inputStartIdx = inputShape.calcFlatIndexFromLocation(inputDeepLoc);
				
				// calculate class probs
				_colors.calculateClassProbs(
						outputData, 
						outputStartIdx, 
						inputData, 
						inputStartIdx);
			}
		}
	}

	@Override
	public void learn() {
		
		final Tensor input = getInputNotNull();
		
		_colors.clearObservations();
		
		final int[] outputDeepIdxs = new int[3];
		final int[] inputDeepIdxs = new int[3];
		final Location outputDeepLoc = new Location(outputDeepIdxs);
		final Location inputDeepLoc = new Location(inputDeepIdxs);
		final double[] outputData = this.output.data();
		final double[] inputData = input.data();
		
		for (int i=0; i<this.inputHeight;i++) {
			outputDeepIdxs[0] = i;
			inputDeepIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				outputDeepIdxs[1] = j;
				inputDeepIdxs[1] = j;
				
				// get flat indices
				final int outputStartIdx = outputShape.calcFlatIndexFromLocation(outputDeepLoc);
				final int inputStartIdx = inputShape.calcFlatIndexFromLocation(inputDeepLoc);
				
				// calculate class probs
				_colors.addObservation(
						outputData, 
						outputStartIdx, 
						inputData, 
						inputStartIdx);
			}
		}
	}

	@Override
	public void dream() {
		
		final Tensor input = getInputNotNull();
		
		final int[] outputDeepIdxs = new int[3];
		final int[] inputDeepIdxs = new int[3];
		final Location outputDeepLoc = new Location(outputDeepIdxs);
		final Location inputDeepLoc = new Location(inputDeepIdxs);
		final double[] outputData = this.output.data();
		final double[] inputData = input.data();
		
		for (int i=0; i<this.inputHeight;i++) {
			outputDeepIdxs[0] = i;
			inputDeepIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				outputDeepIdxs[1] = j;
				inputDeepIdxs[1] = j;
				
				// get flat indices
				final int outputStartIdx = outputShape.calcFlatIndexFromLocation(outputDeepLoc);
				final int inputStartIdx = inputShape.calcFlatIndexFromLocation(inputDeepLoc);
				
				// calculate class probs
				_colors.calculateChannelMeans(
						outputData, 
						outputStartIdx, 
						inputData, 
						inputStartIdx);
			}
		}
	}

}
