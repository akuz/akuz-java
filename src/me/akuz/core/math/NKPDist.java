package me.akuz.core.math;

import java.text.DecimalFormat;

/**
 * Normal distribution with known precision.
s *
 */
public final class NKPDist {
	
	private final double _priorMyu;
	private final double _priorMyuPrecision;
	private final double _precision;
	private double _sumWeights;
	private double _sum;
	
	public NKPDist(double priorMyu, double priorMyuPrecision, double precision) {
		_priorMyu = priorMyu;
		_priorMyuPrecision = priorMyuPrecision;
		_precision = precision;
	}
	
	public double getPrecision() {
		return _precision;
	}
	
	public void addObservation(double value) {
		addObservation(value, 1.0);
	}
	
	public void addObservation(double value, double weight) {
		_sumWeights += weight;
		_sum += value * weight;
	}
	
	public double getPosteriorMyu() {
		return (_priorMyu * _priorMyuPrecision + _precision * _sum) / (_priorMyuPrecision + _sumWeights * _precision);
	}
	
	public double getPosteriorMyuPrecision() {
		return _priorMyuPrecision + _sumWeights * _precision;
	}
	
	public double getPredictiveMyu() {
		return getPosteriorMyu();
	}
	public double getPredictiveVariance() {
		return 1.0/getPosteriorMyuPrecision() + 1.0/getPrecision();
	}
	
	public double getProb(double value) {
		return GaussianFunc.pdf(getPredictiveMyu(), getPredictiveVariance(), value);
	}
	
	public double getLogProb(double value) {
		return GaussianFunc.logPdf(getPredictiveMyu(), getPredictiveVariance(), value);
	}

	@Override
	public String toString() {
		DecimalFormat fmt = new DecimalFormat("' '0.00000000;'-'0.00000000");
		double myu = getPredictiveMyu();
		double sigma = Math.sqrt(getPredictiveVariance());
		return "<NKP> mean: " + fmt.format(myu) + ",  std: " + fmt.format(sigma);
	}
}
