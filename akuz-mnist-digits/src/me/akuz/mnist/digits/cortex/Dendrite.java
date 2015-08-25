package me.akuz.mnist.digits.cortex;

import me.akuz.core.math.GammaFunction;
import me.akuz.core.math.StatsUtils;

public final class Dendrite {
	
	private static final double WEIGHTS_INIT_START = 0.9;
	private static final double WEIGHTS_INIT_RANGE = 0.2;
	
	private final double[] _weights;
	private double _weightsSumLogGamma;

	// In the future:
	// perhaps could use conjugate prior to Gamma
	// at each dimension, to account for the certainty
	// of each connection (more certain - more important)?
	// otherwise each 0 is as important as 1, and
	// uncertainty cannot be expressed

	public Dendrite(final int nextColumnHeight) {
		
		_weights = new double[nextColumnHeight];
		
		for (int i=0; i<_weights.length; i++) {
			_weights[i] = 
					WEIGHTS_INIT_START + 
					WEIGHTS_INIT_RANGE / (_weights.length - 1) * i;
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
	
	public double calculateBottomLogLike(final Column column) {
		
		double logLike = 0.0;
		
		final Neuron[] neurons = column.getNeurons();
		
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
}
