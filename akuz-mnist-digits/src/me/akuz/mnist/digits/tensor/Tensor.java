package me.akuz.mnist.digits.tensor;

public interface Tensor {
	
	double get(Location location);
	
	void set(Location location, double value);

}
