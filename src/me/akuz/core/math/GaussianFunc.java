package me.akuz.core.math;

public final class GaussianFunc {

	public static final double pdf(double mean, double variance, double value) {
		
		return 1.0 / Math.sqrt(2.0 * Math.PI * variance) * Math.exp(- Math.pow(value - mean, 2.0) / 2.0 / variance);
	}
	
	public static final double logPdf(double mean, double variance, double value) {

		return - 0.5 * Math.log(2.0 * Math.PI * variance) - Math.pow(value - mean, 2.0) / 2.0 / variance;
	}
}
