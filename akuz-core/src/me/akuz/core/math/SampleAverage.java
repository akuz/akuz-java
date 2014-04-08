package me.akuz.core.math;

public final class SampleAverage {
	
	private int _count;
	private double _average;
	
	public SampleAverage() {
		reset();
	}
	
	public void add(double value) {
		if (_count == 0) {
			_average = value;
		} else {
			_average = _average / (_count + 1.0) * _count + value / (_count + 1.0);
		}
		_count += 1;
	}
	
	public double getMean() {
		return _average;
	}
	
	public int getCount() {
		return _count;
	}
	
	public void reset() {
		_count = 0;
		_average = Double.NaN;
	}

}
