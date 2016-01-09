package me.akuz.mnist.digits.tensor;

public abstract class Tensor {
	
	public final int ndim;
	public final Shape shape;
	
	public Tensor(final Shape shape) {
		this.ndim = shape.ndim;
		this.shape = shape;
	}
	
	public abstract double get(Location location);
	
	public abstract void set(Location location, double value);

}
