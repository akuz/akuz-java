package me.akuz.mnist.digits.hrp.prob;

import java.util.Arrays;

import org.apache.commons.lang3.NotImplementedException;

public final class DiscreteWithDirPrior implements DiscreteDist {
	
	private final double[] _values;
	private final double[] _observations;
	
	public DiscreteWithDirPrior(
			final double[] values,
			final double alpha) {
		
		if (values == null || values.length == 0) {
			throw new IllegalArgumentException("Argument values must contain at least one value");
		}
		
		_values = values;
		_observations = new double[values.length];
		Arrays.fill(_observations, alpha);
	}
	
	public void addObservation(final int i) {
		_observations[i] += 1.0;
	}
	
	public void addObservation(final int i, final double weight) {
		_observations[i] += weight;
	}
	
	public void removeObservation(final int i) {
		_observations[i] -= 1.0;
	}
	
	public void removeObservation(final int i, final double weight) {
		_observations[i] -= weight;
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
		throw new NotImplementedException("Normalized probability is not supported for this distribution");
	}

	@Override
	public double getProb(int i) {
		throw new NotImplementedException("Normalized probability is not supported for this distribution");
	}

	@Override
	public double getLogLike(int i) {
		return Math.log(_observations[i]);
	}

}
