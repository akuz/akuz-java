package me.akuz.core.math;

public final class WeightedAverage {
	
	private static final double EPSILON = 0.000000001;
	private double _sumWeightedValues;
	private double _sumWeights;
	private double _average;
	
	public WeightedAverage() {
		reset();
	}
	
	public final void reset() {
		_sumWeightedValues = Double.NaN;
		_sumWeights = 0;
		_average = 0;
	}
	
	public final void add(double value) {
		add(value, 1.0);
	}

	public final void add(double value, double weight) {
		if (weight <= EPSILON) {
			return;
			// throw new IllegalArgumentException("Weight must be more positive (>" + EPSILON + ")");
		}
		if (Double.isNaN(_sumWeightedValues)) {
			_sumWeightedValues = value * weight;
			_sumWeights = weight;
		} else {
			_sumWeightedValues += value * weight;
			_sumWeights += weight;
		}
		_average = _sumWeightedValues / _sumWeights;
	}
	
	public final void remove(double value) {
		remove(value, 1.0);
	}
	
	public final void remove(double value, double weight) {
		if (weight <= EPSILON) {
			return;
			// throw new IllegalArgumentException("Weight must be more positive (>" + EPSILON + ")");
		}
		_sumWeightedValues -= value * weight;
		_sumWeights -= weight;
		if (_sumWeights <= EPSILON) {
			reset();
		} else {
			_average = _sumWeightedValues / _sumWeights;
		}
	}
	
	public final double getSumWeightedValues() {
		return _sumWeightedValues;
	}
	
	public final double getSumWeights() {
		return _sumWeights;
	}
	
	public final double get() {
		return _average;
	}

}
