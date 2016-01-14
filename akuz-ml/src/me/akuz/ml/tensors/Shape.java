package me.akuz.ml.tensors;

/**
 * Shape of a Tensor.
 *
 */
public final class Shape {
	
	/**
	 * Number of dimensions.
	 */
	public final int ndim;
	
	/**
	 * Sizes of each dimension.
	 */
	public final int[] sizes;
	
	/**
	 * Total size of the shape.
	 */
	public final int size;

	/**
	 * Multipliers for location indexing.
	 */
	public final int[] multipliers;
	
	
	/**
	 * Create a shape with the specified 
	 * array of sizes for each dimension.
	 */
	public Shape(int[] sizes) {
		TensorUtils.checkNotEmpty(sizes);
		this.ndim = sizes.length;
		this.sizes = sizes;
		int size = 1;
		for (int i=0; i<sizes.length; i++) {
			final int dim_size = sizes[i];
			size *= dim_size;
		}
		this.multipliers = new int[sizes.length];
		this.multipliers[sizes.length - 1] = 1;
		for (int i=sizes.length-2; i>=0; i--) {
			this.multipliers[i] = this.multipliers[i+1] * sizes[i+1];
		}
		this.size = size;		
	}
	
	/**
	 * Create a shape with the specified 
	 * sizes for each dimension.
	 */
	public Shape(Integer... sizes) {
		this(TensorUtils.unwrapIntegerArray(sizes));
	}
	
	/**
	 * Calculate flat index from location.
	 */
	public int calcFlatIndexFromLocation(final Location location) {
		TensorUtils.checkNdimsMatch(this.ndim, location.ndim, "requested location doesn't match tensor shape");
		int flatIndex = 0;
		final int[] indices = location.indices;
		for (int i=0; i<indices.length; i++) {
			final int index = indices[i];
			if (index < 0 || index >= this.sizes[i]) {
				throw new IndexOutOfBoundsException(
					"Index " + index + " in dimension " + i +
					" is out of bounds (dimension size " + 
					this.sizes[i] + ")");
			}
			flatIndex += index * this.multipliers[i];
		}
		return flatIndex;
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Shape)) {
			return false;
		}
		final Shape other = (Shape)obj;
		if (this.ndim != other.ndim) {
			return false;
		}
		final int[] thisSizes = this.sizes;
		final int[] otherSizes = other.sizes;
		for (int i=0; i<thisSizes.length; i++) {
			if (thisSizes[i] != otherSizes[i]) {
				return false;
			}
		}
		return true;
	}

}
