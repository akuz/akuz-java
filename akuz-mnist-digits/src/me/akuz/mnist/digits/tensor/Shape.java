package me.akuz.mnist.digits.tensor;

/**
 * Shape of a Tensor.
 *
 */
public final class Shape {
	
	private final int[] _dims;
	private final int _size;
	
	public Shape(Integer... dims) {
		if (dims.length == 0) {
			throw new IllegalArgumentException(
				"Shape must have at least one dimension");
		}
		_dims = new int[dims.length];
		int size = 1;
		for (int i=0; i<dims.length; i++) {
			final int dim_size = dims[i];
			_dims[i] = dim_size;
			size *= dim_size;
		}
		_size = size;
	}
	
	public int ndim() {
		return _dims.length;
	}

	public int[] dims() {
		return _dims;
	}
	
	public int size() {
		return _size;
	}
	
	public void checkSameNdim(final Location location) {
		if (_dims.length != location.ndim()) {
			throw new IllegalArgumentException(
				"Location " + location + 
				" is inconsistent with" + 
				" shape " + this);
		}
	}
	
	public boolean contains(final Location location) {
		checkSameNdim(location);
		final int[] indices = location.indices();
		for (int i=0; i<indices.length; i++) {
			final int dim_size = _dims[i];
			final int loc = indices[i];
			if (loc < dim_size || loc >= dim_size) {
				return false;
			}
		}
		return true;
	}
	
	public boolean endsWith(final Location location) {
		checkSameNdim(location);
		final int[] indices = location.indices();
		for (int i=0; i<indices.length; i++) {
			final int dim_size = _dims[i];
			final int loc = indices[i];
			if (loc != dim_size) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i=0; i<_dims.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(_dims[i]);
		}
		sb.append(")");
		return sb.toString();
	}

}
