package me.akuz.core.math;

import Jama.Matrix;

public final class GaussianFunc {

	public static final double pdf(double mean, double variance, double value) {
		
		return 1.0 / Math.sqrt(2.0 * Math.PI * variance) * Math.exp(- Math.pow(value - mean, 2.0) / 2.0 / variance);
	}
	
	public static final double logPdf(double mean, double variance, double value) {

		return - 0.5 * Math.log(2.0 * Math.PI * variance) - Math.pow(value - mean, 2.0) / 2.0 / variance;
	}
	
	public static final double calcLogUnnormalizedPdf(Matrix mean, Matrix inverseCov, Matrix value) {
		
		if (mean == null || mean.getRowDimension() == 0) {
			throw new IllegalArgumentException("Mean must not be null or empty");
		}
		if (mean.getColumnDimension() != 1) {
			throw new IllegalArgumentException("Mean must be a column vector");
		}
		if (value == null || value.getRowDimension() == 0) {
			throw new IllegalArgumentException("Value must not be null or empty");
		}
		if (value.getColumnDimension() != 1) {
			throw new IllegalArgumentException("Value must be a column vector");
		}
		if (mean.getRowDimension() != value.getRowDimension()) {
			throw new IllegalArgumentException("Inconsistent dimensions of mean and value");
		}
		if (mean.getRowDimension() != inverseCov.getRowDimension()) {
			throw new IllegalArgumentException("Inconsistent dimensions of mean and inverseCov row count");
		}
		if (mean.getRowDimension() != inverseCov.getColumnDimension()) {
			throw new IllegalArgumentException("Inconsistent dimensions of mean and inverseCov column count");
		}
		
		final Matrix diff = value.minus(mean);
		
		return - 0.5 * diff.transpose().times(inverseCov).times(diff).get(0, 0);
	}

	public static double calcLogFullRankNormalizer(Matrix mean, Matrix cov) {

		if (mean == null || mean.getRowDimension() == 0) {
			throw new IllegalArgumentException("Mean must not be null or empty");
		}
		if (mean.getColumnDimension() != 1) {
			throw new IllegalArgumentException("Mean must be a column vector");
		}
		if (mean.getRowDimension() != cov.getRowDimension()) {
			throw new IllegalArgumentException("Inconsistent dimensions of mean and cov row count");
		}
		if (mean.getRowDimension() != cov.getColumnDimension()) {
			throw new IllegalArgumentException("Inconsistent dimensions of mean and cov column count");
		}

		return - 0.5 * mean.getRowDimension() * Math.log(2*Math.PI) - 0.5 * Math.log(cov.det());
	}

	public static double calcLogPseudoNormalizer(Matrix mean, Matrix cov) {

		if (mean == null || mean.getRowDimension() == 0) {
			throw new IllegalArgumentException("Mean must not be null or empty");
		}
		if (mean.getColumnDimension() != 1) {
			throw new IllegalArgumentException("Mean must be a column vector");
		}
		if (mean.getRowDimension() != cov.getRowDimension()) {
			throw new IllegalArgumentException("Inconsistent dimensions of mean and cov row count");
		}
		if (mean.getRowDimension() != cov.getColumnDimension()) {
			throw new IllegalArgumentException("Inconsistent dimensions of mean and cov column count");
		}
		
		double det = StatsUtils.pseudoDeterminant(cov, 2.0 * Math.PI);

		return - 0.5 * Math.log(det);
	}
}
