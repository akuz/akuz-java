package me.akuz.core.math;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;

/**
 * Normal Inverse-Gamma distribution.
 *
 */
public final class NIGDist implements Cloneable, Distribution {
	
	private final double _lyamdaPrior;
	private final double _vegaPrior;
	private final double _alphaPrior;
	private final double _betaPrior;
	
	private double _lyamdaPosterior;
	private double _vegaPosterior;
	private double _alphaPosterior;
	private double _betaPosterior;
	
	private double _obsN;
	private double _obsAvg;
	private double _obsSqAvg;
	
	public NIGDist(
			double priorMean, 
			double priorMeanSamples, 
			double priorVariance, 
			double priorVarianceSamples) {
		
		_lyamdaPrior = priorMean;
		_vegaPrior = priorMeanSamples;
		_alphaPrior = priorVarianceSamples / 2.0;
		_betaPrior = priorVariance * priorVarianceSamples / 2.0;
		
		reset();
	}

	public double getLyamda() {
		return _lyamdaPosterior;
	}
	
	public double getVega() {
		return _vegaPosterior;
	}
	
	public double getAlpha() {
		return _alphaPosterior;
	}
	
	public double getBeta() {
		return _betaPosterior;
	}

	public double getMeanMode() {
		return _lyamdaPosterior;
	}
	
	public double getVarianceMode() {
		return _betaPosterior / (_alphaPosterior + 1.0);
	}
	
	@Override
	public void reset() {
		
		_lyamdaPosterior = _lyamdaPrior;
		_vegaPosterior = _vegaPrior;
		_alphaPosterior = _alphaPrior;
		_betaPosterior = _betaPrior;

		_obsN = 0;
		_obsAvg = 0;
		_obsSqAvg = 0;
	}

	@Override
	public void addObservation(double value) {
		addObservation(value, 1);
	}
	
	@Override
	public void addObservation(double value, double weight) {
		if (weight <= 0) {
			throw new InvalidParameterException("Parameter weight should be positive");
		}
		_obsN += weight;
		if (Double.isNaN(_obsN)) {
			throw new IllegalStateException("Internal error");
		}
		_obsAvg = _obsAvg / _obsN * (_obsN-weight) + weight * value / _obsN;
		if (Double.isNaN(_obsAvg)) {
			throw new IllegalStateException("Internal error");
		}
		_obsSqAvg = _obsSqAvg / _obsN * (_obsN-weight) + weight * Math.pow(value, 2) / _obsN;
		if (Double.isNaN(_obsSqAvg)) {
			throw new IllegalStateException("Internal error");
		}
		updatePosteriors();
	}
	
	@Override
	public void removeObservation(double value) {
		removeObservation(value, 1);
	}
	
	@Override
	public void removeObservation(double value, double weight) {
		if (weight <= 0) {
			throw new InvalidParameterException("Parameter weight should be positive");
		}
		double newObsN = _obsN - weight;
		if (newObsN < -0.000001) { // take care of numerical rounding issues
			throw new IllegalStateException("Removing more observations than was added before");
		}
		if (newObsN < 0) {
			newObsN = 0;
		}
		double oldObsN = _obsN;
		_obsN = newObsN;
		if (Double.isNaN(_obsN)) {
			throw new IllegalStateException("Internal error");
		}
		if (newObsN > 0) {
			_obsAvg = _obsAvg / newObsN * oldObsN - weight * value / newObsN;
		} else {
			_obsAvg = 0.0;
		}
		if (Double.isNaN(_obsAvg)) {
			throw new IllegalStateException("Internal error");
		}
		if (newObsN > 0) {
			_obsSqAvg = _obsSqAvg / newObsN * oldObsN - weight * Math.pow(value, 2) / newObsN;
		} else {
			_obsSqAvg = 0.0;
		}
		if (Double.isNaN(_obsSqAvg)) {
			throw new IllegalStateException("Internal error");
		}
		updatePosteriors();
	}
	
	private void updatePosteriors() {
		_lyamdaPosterior = (_vegaPrior*_lyamdaPrior + _obsN*_obsAvg) / (_vegaPrior + _obsN);
		if (Double.isNaN(_lyamdaPosterior)) {
			throw new IllegalStateException("Internal error");
		}
		_vegaPosterior = _vegaPrior + _obsN;
		if (Double.isNaN(_vegaPosterior)) {
			throw new IllegalStateException("Internal error");
		}
		_alphaPosterior = _alphaPrior + _obsN / 2.0;
		if (Double.isNaN(_alphaPosterior)) {
			throw new IllegalStateException("Internal error");
		}
		_betaPosterior = _betaPrior + _obsN / 2.0 * (_obsSqAvg - Math.pow(_obsAvg, 2)) + _obsN*_vegaPrior / (_obsN + _vegaPrior) / 2.0 * Math.pow(_obsAvg - _lyamdaPrior, 2);
		if (Double.isNaN(_betaPosterior)) {
			throw new IllegalStateException("Internal error");
		}
	}
	
	public double getProb(double value) {

		// we now have posterior over myu and sigma
		// we need to calculate probability of sample value
		// we integrated out myu and sigma to get the below

		final double help1 = (_vegaPosterior*_lyamdaPosterior*_lyamdaPosterior + value*value)/(1 + _vegaPosterior);
		final double help2 = (_vegaPosterior*_lyamdaPosterior + value)/(1 + _vegaPosterior);
		final double betaStar = _betaPosterior + (_vegaPosterior + 1) / 2.0 * (help1 - help2*help2);
		
		final double gammaFraction = GammaApprox.approxGammaPlusHalfByGammaFraction(_alphaPosterior);
		
		final double powerFraction = Math.pow(Math.pow(_betaPosterior, -2.0*_alphaPosterior/(2.0*_alphaPosterior + 1)) * betaStar, -_alphaPosterior - 0.5);
		
		final double prob = Math.sqrt(_vegaPosterior/(1 + _vegaPosterior)) / Math.sqrt(2*Math.PI) * powerFraction * gammaFraction;
		
		if (Double.isNaN(prob)) {
			throw new IllegalStateException("Internal error");
		}
		return prob;
	}
	
	public String toString() {
		DecimalFormat fmt = new DecimalFormat("' '0.00000000;'-'0.00000000");
		double meanMode = getMeanMode();
		double varianceMode = getVarianceMode();
		double meanVar = varianceMode / _vegaPosterior;
		double meanStd = Math.sqrt(meanVar);
		return "<NIG> mean: " + fmt.format(meanMode) + ",  std: " + fmt.format(Math.sqrt(varianceMode)) + "  (s:" + fmt.format(meanStd) + ", a:" + fmt.format(_alphaPosterior) + ", b:" + fmt.format(_betaPosterior) + ")";
	}
	
	public NIGDist clone() {
		try {
			return (NIGDist)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
