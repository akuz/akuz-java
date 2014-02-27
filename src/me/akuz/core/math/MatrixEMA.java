package me.akuz.core.math;

import Jama.Matrix;

/**
 * Exponential moving average of matrices.
 *
 */
public final class MatrixEMA {
	
	private final Matrix _m;
	private final double _halfLife;
	private final double _decayRate;
	private double _timeFromInit;
	
	public MatrixEMA(final Matrix mInit, final double halfLife) {
		if (halfLife <= 0) {
			throw new IllegalArgumentException("Half-life must be positive");
		}
		_m = (Matrix)mInit.clone();
		_halfLife = halfLife;
		_decayRate = Math.log(2) / _halfLife;
	}
	
	public final void add(Matrix mValue, double deltaTime) {
		if (deltaTime <= 0) {
			throw new IllegalArgumentException("Delta time must be positive");
		}
		if (_m.getRowDimension() != mValue.getRowDimension()) {
			throw new IllegalArgumentException("Row dimensions do not match");
		}
		if (_m.getColumnDimension() != mValue.getColumnDimension()) {
			throw new IllegalArgumentException("Column dimensions do not match");
		}
		final double decay = Math.exp(-_decayRate / deltaTime);
		for (int i=0; i<_m.getRowDimension(); i++) {
			for (int j=0; j<_m.getColumnDimension(); j++) {
				_m.set(i, j, _m.get(i, j) * decay + (1 - decay) * mValue.get(i, j));
			}
		}
		_timeFromInit += deltaTime;
	}
	
	public Matrix getMatrix() {
		return _m;
	}
	
	public double getTimeFromInit() {
		return _timeFromInit;
	}
}
