package me.akuz.core.math;

import Jama.Matrix;

/**
 * A matrix of Dirichlet distributions (each distribution is a column).
 *
 */
public final class VertDirMatrix {
	
	private final Matrix   _mAlphaPrior;
	private final Matrix   _mSumAlphaPrior;
	private       SpMatrix _mObservations;
	private       SpVector _mSumObservations;

	/**
	 * Initialize with a matrix of prior alphas (can be asymmetric).
	 */
	public VertDirMatrix(Matrix mAlphaPrior, Matrix mSumAlphaPrior) {
		if (mAlphaPrior.getColumnDimension() != mSumAlphaPrior.getColumnDimension()) {
			throw new IllegalArgumentException("Alpha and sum alpha priors column counts don't match");
		}
		if (mSumAlphaPrior.getRowDimension() != 1) {
			throw new IllegalArgumentException("Sum alpha prior must contain exactly one row");
		}
		_mAlphaPrior = mAlphaPrior;
		_mSumAlphaPrior = mSumAlphaPrior;
		_mObservations = new SpMatrix();
		_mSumObservations = new SpVector();
	}
	
	/**
	 * Reset to prior by calling clear on observation data structures.
	 */
	public final void resetClear() {
		_mObservations.clear();
		_mSumObservations.clear();
	}
	
	/**
	 * Reset to prior by recreating observation data structures.
	 */
	public final void resetMemory() {
		_mObservations = new SpMatrix();
		_mSumObservations = new SpVector();
	}
	
	/**
	 * Add an observed value.
	 */
	public final void addObservation(int i, int j, Double value) {
		_mObservations.add(i, j, value);
		_mSumObservations.add(j, value);
	}
	
	/**
	 * Get posterior probability.
	 */
	public final double getProb(int i, int j) {
		double part = _mAlphaPrior.get(i, j);
		{
			Double obs = _mObservations.get(i, j);
			if (obs != null) {
				part += obs.doubleValue();
			}
		}
		double whole = _mSumAlphaPrior.get(0, j);
		{
			Double sumObs = _mSumObservations.get(j);
			if (sumObs != null) {
				whole += sumObs.doubleValue();
			}
		}
		return part / whole;
	}
	
	public int getRowDimension() {
		return _mAlphaPrior.getRowDimension();
	}
	
	public int getColumnDimension() {
		return _mAlphaPrior.getColumnDimension();
	}

}
