package me.akuz.core.math;

import java.security.InvalidParameterException;


public final class SingleArgLinearFunction implements SingleArgFunction {
	
	private final double _minX;
	private final double _minY;
	private final double _maxX;
	private final double _maxY;
	
	public SingleArgLinearFunction(double minX, double minY, double maxX, double maxY) {
		if (minX > maxX) {
			throw new InvalidParameterException("Invalid X bounds, should have minX <= maxX");
		}
		if (minX == maxX && minY != maxY) {
			throw new InvalidParameterException("Point function (where minX == maxX) should have minY == maxY");
		}
		_minX = minX;
		_minY = minY;
		_maxX = maxX;
		_maxY = maxY;
	}

	public double getValueAt(double x) {
		if (x < _minX) {
			throw new InvalidParameterException("Parameter value for x is lower than the lower bound for X");
		}
		if (x > _maxX) {
			throw new InvalidParameterException("Parameter value for x is larger than the upper bound for X");
		}
		
		if (_minX == _maxX) {
			return _minY;
		}
		
		double share = (x - _minX) / (_maxX - _minX);
		
		return _minY + share * (_maxY - _minY);
	}

}
