package me.akuz.core.math;

/**
 * Implements dense version of the discrete distribution.
 *
 */
public final class DscDistDense implements DscDist {
	
	private final double[] _probs;
	
	/**
	 * Create from an array of probabilities.
	 * The constructor doesn't check whether
	 * they are positive or normalized!
	 */
	public DscDistDense(final double[] probs) {
		if (probs.length <= 1) {
			throw new IllegalArgumentException(
					"Dimensionality must be > 1 " + 
					"(got " + probs.length + ")");
		}
		_probs = probs;
	}

	@Override
	public int getDim() {
		return _probs.length;
	}
	
	@Override
	public double getProb(int index) {
		return _probs[index];
	}

	@Override
	public void addToArray(final double[] arr, final double weight) {
		if (arr.length != _probs.length) {
			throw new IllegalArgumentException(
					"Dimensionalities don't match (" +
					arr.length + " != " + _probs.length + ")");
		}
		for (int i=0; i<_probs.length; i++) {
			arr[i] += weight * _probs[i];
		}
	}

}
