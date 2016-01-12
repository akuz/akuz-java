package me.akuz.ml.tensors;

public final class AddTensor extends Tensor {

	private final Tensor _underlying;
	private final Tensor _data;
	
	public AddTensor(final Tensor underlying) {
		super(underlying.shape);
		_underlying = underlying;
		_data = new DenseTensor(underlying.shape);
	}

	@Override
	public double get(int flatIndex) {
		return _underlying.get(flatIndex) + _data.get(flatIndex);
	}

	@Override
	public double get(Location location) {
		final int flatIndex = this.shape.calcFlatIndexFromLocation(location);
		return _underlying.get(flatIndex) + _data.get(flatIndex);
	}

	@Override
	public void set(int flatIndex, double value) {
		_data.set(flatIndex, value);
	}

	@Override
	public void set(Location location, double value) {
		_data.set(location, value);
	}

	@Override
	public void add(int flatIndex, double value) {
		_data.add(flatIndex, value);
	}

	@Override
	public void add(Location location, double value) {
		_data.add(location, value);
	}

	@Override
	public void fill(double value) {
		_data.fill(value);
	}

}
