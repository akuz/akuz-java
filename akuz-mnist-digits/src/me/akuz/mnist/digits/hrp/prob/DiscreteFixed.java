package me.akuz.mnist.digits.hrp.prob;

public final class DiscreteFixed implements DiscreteDist {
	
	private final double[] _values;
	private final double[] _probs;
	
	public DiscreteFixed(
			final double[] values,
			final double[] probs) {
		
		if (values == null || values.length == 0) {
			throw new IllegalArgumentException("Argument values must contain at least one value");
		}
		if (probs == null || probs.length == 0) {
			throw new IllegalArgumentException("Argument probs must contain at least one value");
		}
		if (values.length != probs.length) {
			throw new IllegalArgumentException("Argument values must be of the same length as probs");
		}
		_values = values;
		_probs = probs;		
	}

	@Override
	public int getDim() {
		return _values.length;
	}

	@Override
	public double[] getValues() {
		return _values;
	}

	@Override
	public double getValue(int i) {
		return _values[i];
	}

	@Override
	public double[] getProbs() {
		return _probs;
	}

	@Override
	public double getProb(int i) {
		return _probs[i];
	}

	@Override
	public double getLogLike(int i) {
		return Math.log(_probs[i]);
	}

}
