package me.akuz.mnist.digits.visor.transform;

import java.util.Arrays;

import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;
import me.akuz.ml.tensors.TensorIterator;
import me.akuz.mnist.digits.visor.VisorLayer;

public final class SplitLastDim extends VisorLayer {
	
	public final Tensor output1;
	public final Tensor output2;
	
	private final int _size1;

	public SplitLastDim(Shape inputShape, int size1) {
		super(inputShape);
		
		if (size1 <= 0) {
			throw new IllegalArgumentException("size1 must be > 0, got " + size1);
		}
		final int lastDimSize = inputShape.sizes[inputShape.ndim-1];
		if (size1 >= lastDimSize) {
			throw new IllegalArgumentException(
					"size1 (got " + size1 + 
					") must be < lastDimSize (" + 
					lastDimSize + ")");
		}
		_size1 = size1;
		
		// init output1
		{
			final int[] sizes = Arrays.copyOf(inputShape.sizes, inputShape.ndim);
			sizes[inputShape.ndim-1] = size1;
			this.output1 = new Tensor(new Shape(sizes));
		}
		
		// init output2
		{
			final int[] sizes = Arrays.copyOf(inputShape.sizes, inputShape.ndim);
			sizes[inputShape.ndim-1] = lastDimSize - size1;
			this.output2 = new Tensor(new Shape(sizes));
		}
		
	}

	@Override
	public void infer(boolean useOutputAsPrior) {
		
		final Tensor input = getInputNotNull();
		final TensorIterator it = new TensorIterator(input.shape);
		final int lastDim = input.shape.ndim-1;
		
		while (it.next()) {
			
			final Location loc = it.loc();
			final int[] indices = loc.indices;
			final int itLastDimIndex = indices[lastDim];

			final double inputValue = input.get(loc);
			if (itLastDimIndex < _size1) {

				this.output1.set(loc, inputValue);

			} else {
				
				indices[lastDim] = itLastDimIndex - _size1;
				this.output2.set(loc, inputValue);
				indices[lastDim] = itLastDimIndex;
			}
		}
	}

	@Override
	public void learn() {
		// nothing to do
	}

	@Override
	public void dream() {
		
		final Tensor input = getInputNotNull();
		final TensorIterator it = new TensorIterator(input.shape);
		final int lastDim = input.shape.ndim-1;
		
		while (it.next()) {
			
			final Location loc = it.loc();
			final int[] indices = loc.indices;
			final int itLastDimIndex = indices[lastDim];
			
			final double inputValue;
			if (itLastDimIndex < _size1) {

				inputValue = this.output1.get(loc);

			} else {
				
				indices[lastDim] = itLastDimIndex - _size1;
				inputValue = this.output2.get(loc);
				indices[lastDim] = itLastDimIndex;
			}
			
			input.set(loc, inputValue);
		}
	}

}
