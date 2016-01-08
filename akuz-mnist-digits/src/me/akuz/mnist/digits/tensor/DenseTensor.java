package me.akuz.mnist.digits.tensor;

/**
 * Dense tensor implementation.
 *
 */
public final class DenseTensor implements Tensor {
	
	private final Shape _shape;
	private final double[] _data;
	
	public DenseTensor(final Shape shape) {
		_shape = shape;
		_data = new double[shape.size()];
	}
	
	public Shape shape() {
		return _shape;
	}

	@Override
	public double get(Integer... location) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double set(Integer... location) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
