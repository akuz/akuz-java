package me.akuz.nlp.cortex;

import java.util.Arrays;

/**
 * Probabilistic character, represented as a 
 * discrete distribution over some symbols.
 *
 */
public final class PChar {
	
	private final int _dim;
	private final double[] _probs;
	private final int _specificIndex;
	
	public PChar(int dim) {
		if (dim <= 1) {
			throw new IllegalArgumentException(
					"Dimensionality must be > 1");
		}
		_dim = dim;
		_probs = new double[dim];
		Arrays.fill(_probs, 1.0 / dim);
		_specificIndex = -1;
	}
	
	/**
	 * Create p-char with specific 
	 * *fixed* symbol index having
	 * probability 1, and the rest
	 * having probability of 0.
	 */
	public PChar(int dim, int specificIndex) {
		if (dim <= 1) {
			throw new IllegalArgumentException(
					"Dimensionality must be > 1");
		}
		if (specificIndex < 0 || specificIndex >= dim) {
			throw new IllegalArgumentException(
					"Specific index must be within [0, " + 
					dim + "), but got " + specificIndex);
		}
		_dim = dim;
		_probs = null;
		_specificIndex = specificIndex;
	}

	public int getDim() {
		return _dim;
	}
	
	public double[] getProbs() {
		if (_probs == null) {
			throw new IllegalStateException(
					"This PChar doesn't store probs " +
					"because is has specific index set");
		}
		return _probs;
	}
	
	public double getProb(int index) {
		if (index < 0 || index >= _dim) {
			throw new IllegalArgumentException(
					"Index must be within [0, " + 
					_dim + "), but got " + index);
		}
		if (_specificIndex >= 0) {
			return _specificIndex == index ? 1.0 : 0.0;
		} else {
			return _probs[index];
		}
	}
	
	/**
	 * Copy the probabilities from the given array.
	 * Only works of the PChar doesn't have a fixed
	 * specific index set (otherwise exception).
	 * 
	 */
	public void setProbsFrom(final double[] probs) {
		if (_specificIndex >= 0) {
			throw new IllegalStateException(
					"PChar has specific index set, " +
					"cannot update probabilities");
		}
		if (_dim != probs.length) {
			throw new IllegalArgumentException(
					"Dimensionalities of PChar (" + _dim + ") " +
					"and assigned probabilities (" + probs.length + ") " +
					"don't match");
		}
		double sum = 0.0;
		for (int i=0; i<_dim; i++) {
			double prob = probs[i];
			_probs[i] = prob;
			sum += prob;
		}
		if (Double.isNaN(sum)) {
			throw new IllegalArgumentException(
					"Some of the probabilities are NaN");
		}
		if (Math.abs(1.0 - sum) < 1e-10) {
			throw new IllegalArgumentException(
					"Probabilities don't sum up to 1.0 (instead " + sum + ")");
		}
	}
}
