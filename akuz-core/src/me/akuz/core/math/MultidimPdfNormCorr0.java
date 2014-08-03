package me.akuz.core.math;

import java.security.InvalidParameterException;

import me.akuz.core.math.MultidimPdf;

public final class MultidimPdfNormCorr0 implements MultidimPdf {
	
	private final double[] _means;
	private final double[] _variances;
	
	public MultidimPdfNormCorr0(double[] means, double[] variances) {
		
		if (means == null || means.length == 0) {
			throw new InvalidParameterException("Means parameter cannot be null or empty");
		}
		if (variances == null || variances.length == 0) {
			throw new InvalidParameterException("Variances parameter cannot be null or empty");
		}
		if (means.length != variances.length) {
			throw new InvalidParameterException("Means and variances arrays must be of the same length");
		}
		_means = means;
		_variances = variances;
	}
	
	public final double getLogProb(double[] values) {
		
		if (values == null || values.length == 0) {
			throw new InvalidParameterException("Values parameter cannot be null or empty");
		}
		if (_means.length != values.length) {
			throw new InvalidParameterException("Values parameter array must be of length " + _means.length + " (got " + values.length + ")");
		}
		
		double logProb = 0;
		double logSqrt2PI = 0.5*Math.log(2*Math.PI);
		for (int i=0; i<_means.length; i++) {
			logProb += - Math.log(Math.sqrt(_variances[i])) - logSqrt2PI - Math.pow(values[i] - _means[i], 2)/2.0/_variances[i];
		}
		
		return logProb;
	}

}
