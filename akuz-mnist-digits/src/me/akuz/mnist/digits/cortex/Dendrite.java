package me.akuz.mnist.digits.cortex;

import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.GammaFunction;
import me.akuz.core.math.StatsUtils;

public final class Dendrite {
	
	private static final double WEIGHTS_INIT_START = 0.49;
	private static final double WEIGHTS_INIT_RANGE = 0.02;
	
	private final double[] _weights;
	private double _weightsSumLogGamma;

	// In the future:
	// perhaps could use conjugate prior to Gamma
	// at each dimension, to account for the certainty
	// of each connection (more certain - more important)?
	// otherwise each 0 is as important as 1, and
	// uncertainty cannot be expressed

	public Dendrite(final Brain brain, final int lowerColumnHeight) {
		
		_weights = new double[lowerColumnHeight];
		
		final double minWeight = brain.getDendriteMinWeight();
		final double maxWeight = brain.getDendriteMaxWeight();
		
		for (int i=0; i<_weights.length; i++) {
			_weights[i] = minWeight +
					(maxWeight - minWeight) *
					(WEIGHTS_INIT_START + WEIGHTS_INIT_RANGE * ThreadLocalRandom.current().nextDouble());
		}
		
		normalizeWeights();
	}

	public double[] getWeights() {
		return _weights;
	}
	
	private void normalizeWeights() {
		
		StatsUtils.normalize(_weights);
		
		_weightsSumLogGamma = 0.0;
		for (int i=0; i<_weights.length; i++) {
			_weightsSumLogGamma += GammaFunction.lnGamma(_weights[i]);
		}
	}
	
	public double calculateLowerLogLike(final Column lowerColumn) {
		
		double logLike = 0.0;
		
		final Neuron[] neurons = lowerColumn.getNeurons();
		
		if (_weights.length != neurons.length) {
			throw new IllegalStateException(
					"Dendrite expected a column of height " +
					_weights.length + ", but got a column " +
					"of height " + neurons.length);
		}
		
		for (int i=0; i<_weights.length; i++) {
			logLike += 
					(_weights[i] - 1.0) * 
					Math.log(neurons[i].getPreviousPotential());
		}
		
		logLike -= _weightsSumLogGamma;
		
		return logLike;
	}

	public void learnTick(final Column lowerColumn, final double learnWeightNow) {

		final Neuron[] lowerNeurons = lowerColumn.getNeurons();

		if (_weights.length != lowerNeurons.length) {
			throw new IllegalStateException(
					"Expected " + _weights.length + " neurons " +
					"in the lower column, but found " + lowerNeurons.length);
		}
		
		for (int w=0; w<_weights.length; w++) {
			
			_weights[w] += learnWeightNow * lowerNeurons[w].getPreviousPotential();
		}
		
		normalizeWeights();

	}
}
