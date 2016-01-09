package me.akuz.mnist.digits.tensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Location within a Tensor.
 *
 */
public final class Location {
	
	private final int[] _indices;
	
	public Location(int[] indices) {
		_indices = indices;
		TensorUtils.checkNotEmpty(_indices);
	}
	
	public Location(Integer... indices) {
		_indices = TensorUtils.unboxIntegerArray(indices);
		TensorUtils.checkNotEmpty(_indices);
	}
	
	public Location add(final Shape shape) {
		
		int[] shapeDims = shape.dims();
		if (_indices.length != shapeDims.length) {
			throw new IllegalArgumentException(
				"Shape " + shape + " doesn't match location " + this);
		}
		final int[] result = new int[shapeDims.length];
		for (int i=0; i<shapeDims.length; i++) {
			result[i] = _indices[i] + shapeDims[i];
		}
		return new Location(result);
	}

	public void set(int dim, int index) {
		_indices[dim] = index;
	}
	
	public int[] indices() {
		return _indices;
	}
	
	public int ndim() {
		return _indices.length;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i=0; i<_indices.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(_indices[i]);
		}
		sb.append("]");
		return sb.toString();
	}

}
