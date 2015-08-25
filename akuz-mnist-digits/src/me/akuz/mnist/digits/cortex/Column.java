package me.akuz.mnist.digits.cortex;

import me.akuz.core.math.StatsUtils;

public final class Column {
	
	private final Neuron[] _neurons;
	
	public Column(final int neuronsPerColumn) {
		_neurons = new Neuron[neuronsPerColumn];
		for (int l=0; l<neuronsPerColumn; l++) {
			_neurons[l] = new Neuron(neuronsPerColumn);
		}
	}

	public Neuron[] getNeurons() {
		return _neurons;
	}
	
	public void preUpdate() {
		for (int i=0; i<_neurons.length; i++) {
			_neurons[i].preUpdate();
		}
	}
	
	public void update(
			final int i,
			final int j,
			final Layer nextLayer) {
		
		// TODO: persistence
		
		// TODO: top log like
		
		// --------------------------
		// bottom-up information flow
		//
		double[] bottomLogLikes = new double[_neurons.length];
		for (int n=0; n<_neurons.length; n++) {
			bottomLogLikes[n] = _neurons[n].calculateBottomLogLike(i, j, nextLayer);
		}
		StatsUtils.logLikesToProbsReplace(bottomLogLikes);
	}
}
