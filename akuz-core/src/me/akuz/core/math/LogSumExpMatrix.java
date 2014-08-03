package me.akuz.core.math;

import Jama.Matrix;

public final class LogSumExpMatrix {
	
	private final double[] _buffer;
	private final Matrix _mLogInit;
	private final Matrix _mLogResult;
	
	public LogSumExpMatrix(Matrix mInitProbs) {
		_buffer = new double[2];
		_mLogInit = new Matrix(mInitProbs.getRowDimension(), mInitProbs.getColumnDimension());
		for (int i=0; i<mInitProbs.getRowDimension(); i++) {
			for (int j=0; j<mInitProbs.getColumnDimension(); j++) {
				double prob = mInitProbs.get(i, j);
				if (prob <= 0) {
					throw new IllegalArgumentException("Initial probabilities matrix should contain only positive values");
				}
				_mLogInit.set(i, j, Math.log(prob));
			}
		}
		_mLogResult = (Matrix)_mLogInit.clone();
	}
	
	public void reset() {
		for (int i=0; i<_mLogInit.getRowDimension(); i++) {
			for (int j=0; j<_mLogInit.getColumnDimension(); j++) {
				_mLogResult.set(i, j, _mLogInit.get(i, j));
			}
		}
	}
	
	public void logSumExp(int i, int j, double log) {
		_buffer[0] = _mLogResult.get(i, j);
		_buffer[1] = log;
		_mLogResult.set(i, j, StatsUtils.logSumExp(_buffer));
	}
	
	public Matrix getLogInit() {
		return _mLogInit;
	}
	
	public Matrix getLogResult() {
		return _mLogResult;
	}

}
