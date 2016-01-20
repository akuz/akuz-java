package me.akuz.ml.tensors;

import java.util.Arrays;

/**
 * Dense tensor containing the data.
 * 
 */
public final class Tensor extends TensorBase {
	
	private final double[] _data;
	
	public Tensor(final Shape shape) {
		
		super(shape);
		
		_data = new double[shape.size];
	}

	@Override
	public double get(int flatIndex) {
		return _data[flatIndex];
	}

	@Override
	public void set(int flatIndex, double value) {
		_data[flatIndex] = value;
	}

	@Override
	public double get(final Location location) {
		return _data[this.shape.calcFlatIndexFromLocation(location)];
	}

	@Override
	public void add(int flatIndex, double value) {
		_data[flatIndex] += value;
	}

	@Override
	public void mul(int flatIndex, double value) {
		_data[flatIndex] *= value;
	}

	@Override
	public void set(final Location location, final double value) {
		_data[this.shape.calcFlatIndexFromLocation(location)] = value;
	}

	@Override
	public void add(final Location location, final double value) {
		_data[this.shape.calcFlatIndexFromLocation(location)] += value;
	}

	@Override
	public void mul(final Location location, final double value) {
		_data[this.shape.calcFlatIndexFromLocation(location)] *= value;
	}

	@Override
	public void fill(final double value) {
		Arrays.fill(_data, value);
	}
	
	public double[] data() {
		return _data;
	}
}
