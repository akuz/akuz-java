package me.akuz.mnist.digits.prob;

public final class GaussFixed implements GaussDist {
	
	private final double _mean;
	private final double _variance;

	public GaussFixed(
			final double mean,
			final double variance) {
		_mean = mean;
		_variance = variance;
	}
	
	@Override
	public final double getMean() {
		return _mean;
	}

	@Override
	public final double getVariance() {
		return _variance;
	}
}
