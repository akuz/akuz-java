package me.akuz.mnist.digits.cortex;

import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.DirDist;

public final class Dendrite {
	
	private static final double WEIGHTS_INIT_START = 9.48;
	private static final double WEIGHTS_INIT_RANGE = 0.02;
	
	private final DirDist _weightsDist;

	// In the future:
	// perhaps could use conjugate prior to Gamma
	// at each dimension, to account for the certainty
	// of each connection (more certain - more important)?
	// otherwise each 0 is as important as 1, and
	// uncertainty cannot be expressed

	public Dendrite(final Brain brain, final int lowerColumnHeight) {
		
		final double alpha = WEIGHTS_INIT_START;
		final double[] data = new double[lowerColumnHeight];
		
		final double minWeight = brain.getDendriteMinWeight();
		final double maxWeight = brain.getDendriteMaxWeight();
		
		for (int i=0; i<data.length; i++) {
			data[i] = minWeight +
					(maxWeight - minWeight) *
					(WEIGHTS_INIT_START + WEIGHTS_INIT_RANGE * ThreadLocalRandom.current().nextDouble());
		}
		
		_weightsDist = new DirDist(data, alpha);
	}

	public DirDist getWeightsDist() {
		return _weightsDist;
	}
	
	public double calculateLowerLogLike(final Column lowerColumn) {
		
		final Neuron[] lowerNeurons = lowerColumn.getNeurons();
		
		if (_weightsDist.getDim() != lowerNeurons.length) {
			throw new IllegalStateException(
					"Dendrite expected a column of height " +
					_weightsDist.getDim() + ", but got a column " +
					"of height " + lowerNeurons.length);
		}
		
		final double[] values = new double[lowerNeurons.length];
		for (int i=0; i<lowerNeurons.length; i++) {
			values[i] = lowerNeurons[i].getPreviousPotential();
		}
		
		final double logLike = _weightsDist.getPosteriorLogProb(values);
		
		return logLike;
	}

	public void learnTick(
			final Column lowerColumn,
			final double learnWeightNow) {

		final Neuron[] lowerNeurons = lowerColumn.getNeurons();

		if (_weightsDist.getDim() != lowerNeurons.length) {
			throw new IllegalStateException(
					"Dendrite expected a column of height " +
					_weightsDist.getDim() + ", but got a column " +
					"of height " + lowerNeurons.length);
		}
		
		final double[] values = new double[lowerNeurons.length];
		for (int i=0; i<lowerNeurons.length; i++) {
			values[i] = lowerNeurons[i].getPreviousPotential();
		}
		
		_weightsDist.addObservation(values, learnWeightNow);
	}
}
