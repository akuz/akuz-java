package me.akuz.mnist.digits.tensor;

/**
 * Dense tensor implementation.
 *
 */
public final class TensorData implements Tensor {
	
	private final Shape _shape;
	private final double[] _data;
	
	public TensorData(final Shape shape) {
		_shape = shape;
		_data = new double[shape.size()];
	}
	
	public Shape shape() {
		return _shape;
	}

	@Override
	public double get(final Location location) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void set(final Location location, final double value) {
		// TODO Auto-generated method stub
	}
	
	public double[] data() {
		return _data;
	}
}
