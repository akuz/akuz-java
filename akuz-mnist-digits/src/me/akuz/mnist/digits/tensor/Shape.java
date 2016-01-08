package me.akuz.mnist.digits.tensor;

/**
 * Shape of a Tensor.
 *
 */
public final class Shape {
	
	private final int[] _dims;
	private int _size;
	
	public Shape() {
		_dims = new int[0];
		computeSize();
	}

	public Shape(Integer... dims) {
		if (dims == null) {
			throw new NullPointerException("Shape dims cannot be null");
		}
		// unbox integers to ints
		_dims = new int[dims.length];
		for (int i=0; i<dims.length; i++) {
			_dims[i] = dims[i];
		}
		computeSize();
	}
	
	public Shape(final int[] dims) {
		if (dims == null) {
			throw new NullPointerException("Shape dims cannot be null");
		}
		_dims = dims;
		computeSize();
	}
	
	private void computeSize() {
		if (_dims.length == 0) {
			_size = 0;
		} else {
			int size = 1;
			for (int i=0; i<_dims.length; i++) {
				size *= _dims[i];
				if (size == 0) {
					break;
				}
			}
			_size = size;
		}
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
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
