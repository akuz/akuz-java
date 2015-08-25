package me.akuz.mnist.digits.cortex;

import me.akuz.core.math.StatsUtils;

public final class Column {
	
	private final Neuron[] _neurons;
	
	public Column(
			final int thisColumnHeight,
			final int lowerColumnHeight) {
		
		_neurons = new Neuron[thisColumnHeight];
		for (int l=0; l<thisColumnHeight; l++) {
			_neurons[l] = new Neuron(lowerColumnHeight);
		}
	}

	public Neuron[] getNeurons() {
		return _neurons;
	}
	
	public void beforeUpdate() {
		for (int i=0; i<_neurons.length; i++) {
			_neurons[i].beforeUpdate();
		}
	}
	
	public void update(
			final Brain brain,
			final int i,
			final int j,
			final Layer nextLayer) {
		
		// TODO: persistence
		// double[] timeProbs = new double[_neurons.length];
		
		// TODO: top log like
		// double[] topLogLikes = new double[_neurons.length];
		
		// --------------------------
		// bottom-up information flow
		//
		double[] bottomLogLikes = new double[_neurons.length];
		for (int n=0; n<_neurons.length; n++) {
			bottomLogLikes[n] = _neurons[n].calculateBottomLogLike(i, j, nextLayer);
		}
		StatsUtils.logLikesToProbsReplace(bottomLogLikes);
	}
	
	public void afterUpdate(final Brain brain) {
		
		// TODO: weights learning
	}
}
