package me.akuz.ml.tensors;

import java.text.DecimalFormat;

/**
 * Base class for tensors.
 * 
 */
public abstract class TensorBase {
	
	public static final DecimalFormat DF = new DecimalFormat("#.########");
	
	/**
	 * Number of dimensions.
	 */
	public final int ndim;
	
	/**
	 * Total size of the tensor.
	 */
	public final int size;
	
	/**
	 * Shape of the tensor.
	 */
	public final Shape shape;
	
	/**
	 * Create tensor of a fixed shape.
	 */
	public TensorBase(final Shape shape) {
		this.ndim = shape.ndim;
		this.size = shape.size;
		this.shape = shape;
	}
	
	/**
	 * Get the value by flat index.
	 */
	public abstract double get(int flatIndex);
	
	/**
	 * Get the value by tensor location.
	 */
	public abstract double get(Location location);
	
	/**
	 * Set the value by flat index.
	 */
	public abstract void set(int flatIndex, double value);
	
	/**
	 * Set the value by tensor location.
	 */
	public abstract void set(Location location, double value);

	/**
	 * Add the value by flat index.
	 */
	public abstract void add(int flatIndex, double value);
	
	/**
	 * Add the value by tensor location.
	 */
	public abstract void add(Location location, double value);

	/**
	 * Multiply with the value by flat index.
	 */
	public abstract void mul(int flatIndex, double value);
	
	/**
	 * Multiply with the value by tensor location.
	 */
	public abstract void mul(Location location, double value);
	
	/**
	 * Fill tensor with the value.
	 */
	public abstract void fill(double value);
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final TensorIterator it = new TensorIterator(this.shape);
		while (it.next()) {
			
			final Location loc = it.loc();
			final int[] idxs = loc.indices;
			
			sb.append("[");
			for (int i=0; i<this.ndim; i++) {
				if (i != 0) {
					sb.append(",");
				}
				sb.append(idxs[i]);
			}
			sb.append("]: ");
			sb.append(DF.format(this.get(loc)));
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

}
