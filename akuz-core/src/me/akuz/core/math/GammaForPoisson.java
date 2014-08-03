package me.akuz.core.math;

import java.io.Serializable;
import java.security.InvalidParameterException;

/**
 * Conjugate Gamma distribution for parameter Lambda of a Poisson distribution.
 *
 */
public final class GammaForPoisson implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private double _alphaPrior;
	private double _betaPrior;
	private double _alphaPosterior;
	private double _betaPosterior;
	
	/**
	 * Prior parameters interpretation: alpha total occurrences in beta intervals.
	 * @param alpha - prior alpha
	 * @param beta - prior beta
	 */
	public GammaForPoisson(double alpha, double beta) {
		if (alpha <= 0) {
			throw new InvalidParameterException("Parameter alpha should be > 0");
		}
		if (beta <= 0) {
			throw new InvalidParameterException("Parameter beta should be > 0");
		}
		_alphaPrior = alpha;
		_betaPrior = beta;
		reset();
	}
	
	public void reset() {
		_alphaPosterior = _alphaPrior;
		_betaPosterior = _betaPrior;
	}
	
	public void replacePrior(double alpha, double beta) {
		
		// remove old prior
		_alphaPosterior -= _alphaPrior;
		_betaPosterior -= _betaPrior;
		
		// set new prior
		_alphaPrior = alpha;
		_betaPrior = beta;
		
		// add new prior
		_alphaPosterior += _alphaPrior;
		_betaPosterior += _betaPrior;
	}
	
	public double getAlphaPrior() {
		return _alphaPrior;
	}
	
	public double getBetaPrior() {
		return _betaPrior;
	}
	
	public double getAlphaPosterior() {
		return _alphaPosterior;
	}
	
	public double getBetaPosterior() {
		return _betaPosterior;
	}
	
	/**
	 * Adds a Poisson observation in one whole interval.
	 * @param k - number of occurrences in an observed interval
	 */
	public void addPoissonObservation(int k) {
		addPoissonObservation(k, 1);
	}
	
	/**
	 * Add Poisson observation in specified interval.
	 * @param k - number of occurrences in specified interval
	 * @param interval - length of the interval
	 */
	public void addPoissonObservation(int k, double interval) {
		if (k < 0) {
			throw new InvalidParameterException("Parameter k should be >= 0");
		}
		if (interval < 0) {
			throw new InvalidParameterException("Parameter interval should be >= 0");
		}
		_alphaPosterior += k;
		_betaPosterior += interval;
	}
	
	/**
	 * Removes a Poisson observation observed in one whole interval.
	 * @param k - number of occurrences in an observed interval
	 */
	public void removePoissonObservation(int k) {
		removePoissonObservation(k, 1);
	}
	
	/**
	 * Removes Poisson observation observed in specified interval.
	 * @param k - number of occurrences in specified interval
	 * @param interval - length of the interval
	 */
	public void removePoissonObservation(int k, double interval) {
		if (k < 0) {
			throw new InvalidParameterException("Parameter k should be >= 0");
		}
		if (interval < 0) {
			throw new InvalidParameterException("Parameter interval should be >= 0");
		}
		double newBetaPosterior = _betaPosterior - interval;
		if (newBetaPosterior - _betaPrior < -0.0000000000001) { // take care of numerical rounding issues
			throw new IllegalStateException("Tryint to remove more observations than was added before");
		}
		if (newBetaPosterior < _betaPrior) {
			newBetaPosterior = _betaPrior;
		}
		_alphaPosterior = _alphaPosterior - k;
		_betaPosterior = newBetaPosterior;
	}
	
	public double getLambdaPosteriorMode() {
		if (_alphaPosterior < 1) {
			throw new IllegalStateException("Not enough observations to compute mode, need at least one observation with non-zero occurrence of the rare event");
		}
		return (_alphaPosterior - 1.0) / _betaPosterior;
	}
	
	public double getLambdaPosteriorMean() {
		return _alphaPosterior / _betaPosterior;
	}
	
	public double getLambdaPosteriorVariance() {
		return _alphaPosterior / _betaPosterior / _betaPosterior;
	}
	
	/**
	 * Computes log predictive probability (with parameters integrated out) of a Poisson observation.
	 * @param k - number of rare events within an interval
	 * @return Log probability of the argument
	 */
	public double getPredictiveLogProb(int k) {
		
		if (k < 0) {
			throw new InvalidParameterException("Parameter k should be >= 0");
		}
		
		// predictive posterior is Negative Binomial
		// with parameters r and p as below
		double r = _alphaPosterior;
		double p = _betaPosterior / (1.0 + _betaPosterior);
		
		// calculate log prob of Negative Binomial
		double logProb = 0;
		logProb += GammaFunction.lnGamma(k + r);
		logProb -= GammaFunction.lnGamma(r);
		logProb += r * Math.log(1.0 - p);
		logProb += k * Math.log(p);
		logProb -= Math.log(1);
		for (int i=2; i<=k; i++) {
			logProb -= Math.log(k);
		}
		
		return logProb;
	}

	public double getPredictivePoissonMean() {
		
		// predictive posterior is Negative Binomial
		// with parameters r and p as below
		double r = _alphaPosterior;
		double p = _betaPosterior / (1.0 + _betaPosterior);
		
		return p * r / (1.0 - p);
	}

	public double getPredictivePoissonMode() {
		
		// predictive posterior is Negative Binomial
		// with parameters r and p as below
		double r = _alphaPosterior;
		double p = _betaPosterior / (1.0 + _betaPosterior);
		
		if (r > 1) {
			return p * (r - 1.0) / (1.0 - p);
		} else {
			return 0.0;
		}
	}
}
