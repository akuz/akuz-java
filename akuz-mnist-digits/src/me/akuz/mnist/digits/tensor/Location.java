package me.akuz.mnist.digits.tensor;

/**
 * Location within a Tensor.
 *
 */
public final class Location {
	
	private final int[] _indices;
	
	public Location(Integer... indices) {
		if (indices.length == 0) {
			throw new IllegalArgumentException(
					"Location must specify at least one index");
		}
		// unbox integers to ints
		_indices = new int[indices.length];
		for (int i=0; i<indices.length; i++) {
			_indices[i] = indices[i];
		}
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
