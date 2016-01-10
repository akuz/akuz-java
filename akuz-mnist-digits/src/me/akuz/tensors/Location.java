package me.akuz.tensors;

/**
 * Location within a Tensor.
 *
 */
public final class Location {
	
	/**
	 * Number of dimensions.
	 */
	public final int ndim;
	
	/**
	 * Indices in each dimension.
	 */
	public final int[] indices;
	
	/**
	 * Create location from an array of indices.
	 */
	public Location(int[] indices) {
		TensorUtils.checkNotEmpty(indices);
		this.ndim = indices.length;
		this.indices = indices;
	}
	
	/**
	 * Create location from specified indices.
	 */
	public Location(Integer... indices) {
		TensorUtils.checkNotEmpty(indices);
		this.ndim = indices.length;
		this.indices = new int[indices.length];
		for (int i=0; i<indices.length; i++) {
			this.indices[i] = indices[i];
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i=0; i<this.indices.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(this.indices[i]);
		}
		sb.append("]");
		return sb.toString();
	}

}
