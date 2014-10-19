package me.akuz.core.math;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Dirichlet distribution.
 *
 */
public final class DirDist {
	
	private final double[] _data;
	private boolean _isNormalized;
	
	public DirDist(final int dim, final double alpha) {
		if (dim < 2) {
			throw new IllegalArgumentException("Dimensionality must be >= 2");
		}
		if (alpha < 0.0) {
			// allowing zero alpha because sometimes we want
			// to represent discrete distribution as 
			throw new IllegalArgumentException("Alpha must be non-negative");
		}
		_data = new double[dim];
		Arrays.fill(_data, alpha);
	}
	
	public int getDim() {
		return _data.length;
	}
	
	public double[] getPosteriorMean() {
		if (_isNormalized == false) {
			throw new IllegalStateException("Cannot get posterior, call normalize() first");
		}
		return _data;
	}
	
	public void addObservation(int index, double value) {
		addObservation(index, value, 1.0);
	}
	
	public void addObservation(int index, double value, double weight) {
		if (_isNormalized) {
			throw new IllegalStateException("Cannot add new observations, already normalized");
		}
		if (weight < 0) {
			throw new IllegalArgumentException("Weight cannot be negative");
		}
		_data[index] += value * weight;
	}
	
	public void addObservation(double[] values) {
		addObservation(values, 1.0);
	}
	
	public void addObservation(double[] values, double weight) {
		if (_isNormalized) {
			throw new IllegalStateException("Cannot add new observations, already normalized");
		}
		if (_data.length != values.length) {
			throw new IllegalArgumentException("Dimensionality does not match");
		}
		if (weight < 0) {
			throw new IllegalArgumentException("Weight cannot be negative");
		}
		for (int i=0; i<values.length; i++) {
			_data[i] += values[i] * weight;
		}
	}

	public void normalize() {
		StatsUtils.normalize(_data);
		_isNormalized = true;
	}
	
	@Override
	public String toString() {
		DecimalFormat fmt = new DecimalFormat("' '0.00000000;'-'0.00000000");
		StringBuilder sb = new StringBuilder();
		sb.append("<DIR (");
		if (_isNormalized) {
			sb.append("normalized");
		} else {
			sb.append("unnormalized");
		}
		sb.append(")>: ");
		for (int i=0; i<_data.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(fmt.format(_data[i]));
		}
		return sb.toString();
	}

}
