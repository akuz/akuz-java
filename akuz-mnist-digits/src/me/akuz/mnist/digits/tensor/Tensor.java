package me.akuz.mnist.digits.tensor;

public interface Tensor {
	
	double get(Integer... location);
	
	double set(Integer... location);

}
