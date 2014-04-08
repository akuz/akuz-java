package me.akuz.core.math;

/**
 * Beta distribution.
 *
 */
public final class BetaDist {
	
	private final double _alphaPrior;
	private final double _betaPrior;
	private double _sumWeights;
	private double _sum;
	
	public BetaDist(double alphaPrior, double betaPrior) {
		if (alphaPrior <= 0) {
			throw new IllegalArgumentException("Alpha prior should be positive");
		}
		if (betaPrior <= 0) {
			throw new IllegalArgumentException("Beta prior should be positive");
		}
		_alphaPrior = alphaPrior;
		_betaPrior = betaPrior;
	}
	
	public void add(double value, double weight) {
		if (value < 0 || value > 1) {
			throw new IllegalArgumentException("Value must be within [0, 1]");
		}
		if (weight < 0) {
			throw new IllegalArgumentException("Weight must be >= 0");
		}
		_sumWeights += weight;
		_sum += value * weight;
	}
	
	public double getAlpha() {
		return _alphaPrior + _sum;
	}
	
	public double getBeta() {
		return _betaPrior + _sumWeights - _sum;
	}
	
	public double getMean() {
		final double alpha = getAlpha();
		final double beta = getBeta();
		return alpha / (alpha + beta);
	}
	
	public double getVar() {
		final double alpha = getAlpha();
		final double beta = getBeta();
		return alpha * beta / Math.pow(alpha + beta, 2) / (alpha + beta + 1);
	}
	
	public void getMeanVar(double[] mv) {
		final double alpha = getAlpha();
		final double beta = getBeta();
		mv[0] = alpha / (alpha + beta);
		mv[1] = alpha * beta / Math.pow(alpha + beta, 2) / (alpha + beta + 1);
	}

}
