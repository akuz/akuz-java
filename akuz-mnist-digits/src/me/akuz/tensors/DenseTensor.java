package me.akuz.tensors;

/**
 * Dense tensor containing the data.
 * 
 */
public final class DenseTensor extends Tensor {
	
	private final double[] _data;
	
	public DenseTensor(final Shape shape) {
		
		super(shape);
		
		_data = new double[shape.size];
	}

	@Override
	public double get(final Location location) {
		return _data[this.shape.calcFlatIndexFromLocation(location)];
	}

	@Override
	public void set(final Location location, final double value) {
		_data[this.shape.calcFlatIndexFromLocation(location)] = value;
	}

	@Override
	public double get(int flatIndex) {
		return _data[flatIndex];
	}

	@Override
	public void set(int flatIndex, double value) {
		_data[flatIndex] = value;
	}
	
	public double[] data() {
		return _data;
	}
}
