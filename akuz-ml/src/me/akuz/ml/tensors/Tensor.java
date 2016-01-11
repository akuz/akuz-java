package me.akuz.ml.tensors;

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

	/**
	 * Add the value by flat index.
	 */
	public abstract void add(int flatIndex, double value);
	
	/**
	 * Add the value by tensor location.
	 */
	public abstract void add(Location location, double value);
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final int[] stack = new int[this.ndim];
		final Location loc = new Location(stack);
		while (true) {
			
			sb.append("[");
			for (int i=0; i<this.ndim; i++) {
				if (i != 0) {
					sb.append(",");
				}
				sb.append(stack[i]);
			}
			sb.append("]: ");
			sb.append(this.get(loc));
			sb.append(System.lineSeparator());
			
			boolean keepGoing = false;
			for (int i=this.ndim-1; i>=0; i--) {
				stack[i] += 1;
				if (stack[i] < this.shape.sizes[i]) {
					keepGoing = true;
					break;
				}
				else {
					stack[i] = 0;
				}
			}
			
			if (!keepGoing) {
				break;
			}
		}
		return sb.toString();
	}

}
