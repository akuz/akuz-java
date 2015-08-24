package me.akuz.mnist.digits.cortex;

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
	
	public void beginUpdate() {
		for (int i=0; i<_neurons.length; i++) {
			_neurons[i].beginUpdate();
		}
	}
}
