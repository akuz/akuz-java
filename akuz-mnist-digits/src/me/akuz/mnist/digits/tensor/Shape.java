package me.akuz.mnist.digits.tensor;

/**
 * Shape of a Tensor.
 *
 */
public final class Shape {
	
	public final int ndim;
	public final int[] sizes;
	public final int size;
	
	public Shape(Integer... sizes) {
		UtilsForTensors.checkNotEmpty(sizes);
		this.ndim = sizes.length;
		this.sizes = new int[sizes.length];
		int size = 1;
		for (int i=0; i<sizes.length; i++) {
			final int dim_size = sizes[i];
			this.sizes[i] = dim_size;
			size *= dim_size;
		}
		this.size = size;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i=0; i<this.sizes.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(this.sizes[i]);
		}
		sb.append(")");
		return sb.toString();
	}

}
