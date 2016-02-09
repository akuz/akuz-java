package me.akuz.mnist.digits.visor.transform;

import me.akuz.ml.tensors.Tensor;
import me.akuz.ml.tensors.TensorIterator;

import java.util.Arrays;

import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.mnist.digits.visor.VisorLayer;

/**
 * Color converter layer with YpCbCr space
 * at the top and sRGB at the bottom, where
 * Yp stands for Y' (gamma adjusted).
 *
 */
public class DiscretizeLastDim extends VisorLayer {

	public final Shape outputShape;
	public final Tensor output;
	
	public DiscretizeLastDim(final Shape inputShape) {
		super(inputShape);
		
		if (inputShape == null) {
			throw new NullPointerException("inputShape");
		}
		int[] outputSizes = Arrays.copyOf(inputShape.sizes, inputShape.ndim+1);
		outputSizes[outputSizes.length-1] = 2;
		this.outputShape = new Shape(outputSizes);
		this.output = new Tensor(outputShape);
	}

	@Override
	public void infer() {
		
		final Tensor input = getInputNotNull();
		
		final int[] outputIndices = new int[this.output.ndim];
		final Location outputLoc = new Location(outputIndices);
		
		TensorIterator it = new TensorIterator(inputShape);
		while (it.next()) {
			
			final int[] indices = it.loc().indices;
			for (int i=0; i<indices.length; i++) {
				outputIndices[i] = indices[i];
			}
			
			final double value = input.get(it.loc());
			
			outputIndices[outputIndices.length-1] = 0;
			output.set(outputLoc, 1.0 - value);
			
			outputIndices[outputIndices.length-1] = 1;
			output.set(outputLoc, value);
		}
	}

	@Override
	public void learn() {
		//nothing to do
	}

	@Override
	public void dream() {
		
		final Tensor input = getInputNotNull();
		
		final int[] outputIndices = new int[this.output.ndim];
		final Location outputLoc = new Location(outputIndices);
		
		TensorIterator it = new TensorIterator(inputShape);
		while (it.next()) {
			
			final int[] indices = it.loc().indices;
			for (int i=0; i<indices.length; i++) {
				outputIndices[i] = indices[i];
			}
			
			outputIndices[outputIndices.length-1] = 1;
			final double value = output.get(outputLoc);
			
			input.set(it.loc(), value);
		}
	}

}
