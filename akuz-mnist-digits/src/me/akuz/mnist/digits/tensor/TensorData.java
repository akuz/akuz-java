package me.akuz.mnist.digits.tensor;

/**
 * Dense tensor implementation.
 *
 */
public final class TensorData extends Tensor {
	
	private final double[] _data;
	
	public TensorData(final Shape shape) {
		
		super(shape);
		
		_data = new double[shape.size];
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
