package me.akuz.ml.tensors;

public final class TensorIterator {
	
	private final int _ndim;
	private final int[] _sizes;
	private final int[] _indices;
	private final Location _loc;
	
	public TensorIterator(final Shape shape) {
		if (shape == null) {
			throw new NullPointerException("shape");
		}
		_ndim = shape.ndim;
		_sizes = shape.sizes;
		_indices = new int[shape.ndim];
		_indices[_ndim-1] = -1;
		_loc = new Location(_indices);
	}
	
	public boolean next() {
		
		for (int i=_ndim-1; i>=0; i--) {
			_indices[i] += 1;
			if (_indices[i] < _sizes[i]) {
				return true;
			}
			_indices[i] = 0;
		}
		
		return false;
	}
	
	public Location loc() {
		return _loc;
	}

}
