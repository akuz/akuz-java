package me.akuz.core.math;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Normal distribution with uncertain mean.
 *
 */
public final class NormalMeanDist implements Cloneable {
	
	private final double _priorMean;
	private final double _priorMeanVariance;
	private final double _knownVariance;
	private int _observationCount;
	private double _sumObservations;
	private NormalDistribution _posteriorDistribution;
	
	public NormalMeanDist(
			final double priorMean, 
			final double priorMeanVariance,
			final double knownVariance) {

		_priorMean = priorMean;
		_priorMeanVariance = priorMeanVariance;
		_knownVariance = knownVariance;
		updatePosteriorDistribution();
	}
	
	public void reset() {
		_observationCount = 0;
		_sumObservations = 0;
		updatePosteriorDistribution();
	}
	
	public void addObservation(double value) {
		_observationCount += 1;
		_sumObservations += value;
		updatePosteriorDistribution();
	}
	
	public void removeObservation(double value) {
		if (_observationCount <= 0) {
			throw new IllegalStateException("No more observations to remove");
		}
		_observationCount -= 1;
		_sumObservations = value;
		updatePosteriorDistribution();
	}
	
	private void updatePosteriorDistribution() {
		final double posteriorMeanVariance = 1.0 / (_observationCount / _knownVariance + 1.0 / _priorMeanVariance);
		final double posteriorMean = posteriorMeanVariance * (_priorMean / _priorMeanVariance + _sumObservations / _knownVariance);
		_posteriorDistribution = new NormalDistribution(posteriorMean, posteriorMeanVariance + _knownVariance);
	}
	
	public double density(double value) {
		return _posteriorDistribution.density(value);
	}
	
	public double probabilityLessOrEqualThan(double value) {
		return _posteriorDistribution.cumulativeProbability(value);
	}
	
	public double probabilityMoreThan(double value) {
		return 1.0 - _posteriorDistribution.cumulativeProbability(value);
	}
	
	public NormalDistribution getPosteriorDistribution() {
		return _posteriorDistribution;
	}
	
	@Override
	public NormalMeanDist clone() {
		NormalMeanDist copy;
		try {
			copy = (NormalMeanDist)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Clone error");
		}
		return copy;
	}

}
