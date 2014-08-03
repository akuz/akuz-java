package me.akuz.ml.signals;

public final class MovAvg {
	
	private final double[] _data;
	private int _lastIndex;
	private int _fillCount;
	private double _avg;
	
	public MovAvg(int size) {
		_data = new double[size];
		_lastIndex = -1;
		_fillCount = 0;
		_avg = 0;
	}
	
	public int size() {
		return _data.length;
	}
	
	public boolean isFilled() {
		return _fillCount == _data.length;
	}
	
	public double getValue() {
		return _avg;
	}
	
	public void add(double value) {
		
		_lastIndex = (_lastIndex + 1) % _data.length;
		if (_fillCount == _data.length) {
			double oldValue = _data[_lastIndex];
			_avg = _avg / (_data.length - 1) * _data.length - oldValue / (_data.length - 1);
		}
		_data[_lastIndex] = value;
		if (_fillCount < _data.length) {
			_fillCount += 1;
		}
		_avg = _avg / _fillCount * (_fillCount - 1) + value / _fillCount;
	}
}
