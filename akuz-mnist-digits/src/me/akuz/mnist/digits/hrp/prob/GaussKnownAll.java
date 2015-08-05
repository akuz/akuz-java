package me.akuz.mnist.digits.hrp.prob;

public final class GaussKnownAll implements Gauss {
	
	private final String _name;
	private double _mean;
	private double _tau;
	
	public GaussKnownAll(
			final String name,
			final double mean,
			final double tau) {
		_name = name;
		_mean = mean;
		_tau = tau;
	}

	@Override
	public String getName() {
		return _name;
	}
	
	@Override
	public double getMean() {
		return _mean;
	}
	
	public void setMean(final double mean) {
		_mean = mean;
	}

	@Override
	public double getTau() {
		return _tau;
	}
	
	public void setTau(final double tau) {
		_mean = tau;
	}
}
