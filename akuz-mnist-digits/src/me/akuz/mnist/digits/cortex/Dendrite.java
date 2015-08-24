package me.akuz.mnist.digits.cortex;

public final class Dendrite {
	
	private final double[] _weights;
	
	public Dendrite(final int neuronsPerColumn) {
		_weights = new double[neuronsPerColumn * 4];
		
		// TODO: initialize weights
	}

	public double[] getWeights() {
		return _weights;
	}
}
