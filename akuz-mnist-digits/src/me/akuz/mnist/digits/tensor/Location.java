package me.akuz.mnist.digits.tensor;

/**
 * A single location within a Tensor.
 *
 */
public final class Location {
	
	public final int ndim;
	public final int[] indices;
	
	public Location(int[] indices) {
		UtilsForTensors.checkNotEmpty(indices);
		this.ndim = indices.length;
		this.indices = indices;
	}
	
	public Location(Integer... indices) {
		UtilsForTensors.checkNotEmpty(indices);
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
	
	// TODO: hash and equals

}
