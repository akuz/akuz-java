package me.akuz.mnist.digits.tensor;

public interface Tensor {
	
	Shape shape();
	
	double get(Location location);
	
	void set(Location location, double value);

}
