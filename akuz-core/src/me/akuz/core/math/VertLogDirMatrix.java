package me.akuz.core.math;

import Jama.Matrix;

/**
 * A matrix of Dirichlet distributions (each distribution is a column),
 * with the underlying values stored as logarithm in order to track
 * smaller values added to the posterior with higher precision.
 *
 */
public final class VertLogDirMatrix {
	
	// buffer for logSumExp()
	private final double[] _buff;
	private final Matrix   _mAlphaPrior;
	private final Matrix   _mLogPosterior;
	private final double[] _mLogSumPosterior;
	
	/**
	 * Initialize with a matrix of priors (can be asymmetric).
	 */
	public VertLogDirMatrix(Matrix mAlphaPrior) {
		_buff = new double[2];
		_mAlphaPrior = mAlphaPrior;
		_mLogPosterior = new Matrix(mAlphaPrior.getRowDimension(), mAlphaPrior.getColumnDimension());
		_mLogSumPosterior = new double[mAlphaPrior.getColumnDimension()];
		reset();
	}
	
	/**
	 * Reset to prior.
	 */
	public final void reset() {
		for (int j=0; j<_mAlphaPrior.getColumnDimension(); j++) {
			double sumAlpha = 0.0;
			for (int i=0; i<_mAlphaPrior.getRowDimension(); i++) {
				double alpha = _mAlphaPrior.get(i, j);
				if (alpha <= 0) {
					throw new IllegalArgumentException("Alpha matrix should be positive");
				}
				sumAlpha += alpha;
				_mLogPosterior.set(i, j, Math.log(alpha));
			}
			_mLogSumPosterior[j] = Math.log(sumAlpha);
		}
	}
	
	/**
	 * Add an observed log value.
	 */
	public final void addLogObservation(int i, int j, double logValue) {
		
		_buff[0] = _mLogPosterior.get(i, j);
		_buff[1] = logValue;
		_mLogPosterior.set(i, j, StatsUtils.logSumExp(_buff));

		_buff[0] = _mLogSumPosterior[j];
		_buff[1] = logValue;
		_mLogSumPosterior[j] = StatsUtils.logSumExp(_buff);
	}
	
	/**
	 * Get posterior probability.
	 */
	public final double getProb(int i, int j) {
		return Math.exp(_mLogPosterior.get(i, j)) / Math.exp(_mLogSumPosterior[j]);
	}

}
