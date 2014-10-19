package me.akuz.core.math;

/**
 * Implements discrete distribution with probability of 1 
 * for a single index, and probability 0 for all others.
 *
 */
public final class DscDistSingle implements DscDist {
	
	private final int _dim;
	private final int _idx;
	
	public DscDistSingle(int dim, int idx) {
		if (dim <= 1) {
			throw new IllegalArgumentException(
					"Dimensionality must be > 1" + 
					", got " + dim);
		}
		if (idx < 0 || idx >= dim) {
			throw new IllegalArgumentException(
					"Index must be within [0, " + 
					dim + "), got " + idx);
		}
		_dim = dim;
		_idx = idx;
	}
	
	@Override
	public int getDim() {
		return _dim;
	}

	@Override
	public double getProb(int index) {
		return index == _idx ? 1.0 : 0.0;
	}
	
	public int getIdx() {
		return _idx;
	}

	@Override
	public void addToArray(final double[] arr, final double weight) {
		if (arr.length != _dim) {
			throw new IllegalArgumentException(
					"Dimensionalities don't match (" +
					arr.length + " != " + _dim + ")");
		}
		arr[_idx] += weight;
	}

}
