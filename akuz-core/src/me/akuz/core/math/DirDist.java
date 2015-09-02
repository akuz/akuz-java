package me.akuz.core.math;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Dirichlet distribution.
 *
 */
public final class DirDist {
	
	private final double _alpha;
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
		_alpha = alpha;
		_data = new double[dim];
		Arrays.fill(_data, alpha);
	}
	
	public DirDist(final double[] data, final double alpha) {
		_alpha = alpha;
		_data = data;
	}
	
	public DirDist(final DirDist unnormalized) {
		_alpha = unnormalized._alpha;
		_data = Arrays.copyOf(unnormalized._data, unnormalized._data.length);
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
	
	public double getPosteriorLogProb(final double[] value) {
		if (_isNormalized) {
			throw new IllegalStateException("Cannot get posterior log prob, already normalized");
		}
		if (value == null) {
			throw new NullPointerException("value");
		}
		if (value.length != _data.length) {
			throw new IllegalArgumentException(
					"Expected dimensionality " + _data.length + 
					", but the value has dimensionality " + value.length);
		}
		
		double sum = 0.0;
		double sumLogLike = 0.0;
		double sumLogGamma = 0.0;
		for (int i=0; i<_data.length; i++) {
			final double x = value[i];
			if (!Double.isNaN(x)) {
				sum += _data[i];
				sumLogLike += (_data[i] - 1.0) * Math.log(x);
				sumLogGamma += GammaFunction.lnGamma(_data[i]);
			}
		}
		
		final double logProb = GammaFunction.lnGamma(sum) - sumLogGamma + sumLogLike;
		
		if (Double.isNaN(logProb)) {
			throw new IllegalStateException("Log likeprobabilitylihood is NAN");
		}
		return logProb;
	}

	public double[] getUnnormalisedPosteriorMean() {
		return _data;
	}

	public double getUnnormalisedPosteriorMean(int index) {
		return _data[index];
	}
	
	public double getPosteriorPredictiveProb(int index) {
		if (_isNormalized == false) {
			throw new IllegalStateException("Cannot get posterior, call normalize() first");
		}
		return _data[index];
	}
	
	public void addObservation(int index, double value) {
		addObservation(index, value, 1);
	}
	
	public void addObservation(int index, double value, double weight) {
		if (_isNormalized) {
			throw new IllegalStateException("Cannot add new observations, already normalized");
		}
		if (weight < 0) {
			throw new IllegalArgumentException("Observation weight cannot be negative");
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
	
	public void reset() {
		Arrays.fill(_data, _alpha);
		_isNormalized = false;
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
			sb.append(i+1);
			sb.append(":");
			sb.append(fmt.format(_data[i]));
		}
		return sb.toString();
	}

}
