package me.akuz.tensors;

/**
 * Base class for tensors.
 * 
 */
public abstract class Tensor {
	
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
	public Tensor(final Shape shape) {
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

}
