package me.akuz.mnist.digits.tensor;

public final class TensorView implements Tensor {
	
	private final Tensor _underlying;
	private final Location _start;
	private final Shape _shape;
	
	public TensorView(
			final Tensor underlying,
			final Location start,
			final Shape shape) {
		
		_underlying = underlying;
		_start = start;
		_shape = shape;
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

	@Override
	public Shape shape() {
		// TODO Auto-generated method stub
		return null;
	}

}
