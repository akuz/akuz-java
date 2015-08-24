package me.akuz.mnist.digits.cortex;

public final class Neuron {
	
	private double _parentPotential;
	private double _currentPotential;
	private double _previousPotential;

	private final Dendrite[] _dendrites;
	
	public Neuron(final int neuronsPerColumn) {
		_dendrites = new Dendrite[4];
		for (int i=0; i<_dendrites.length; i++) {
			_dendrites[i] = new Dendrite(neuronsPerColumn);
		}
	}
	
	public double getParentPotential() {
		return _parentPotential;
	}
	
	public double getCurrentPotential() {
		return _currentPotential;
	}
	
	public double getPreviousPotential() {
		return _previousPotential;
	}
	
	public Dendrite[] getDendrites() {
		return _dendrites;
	}
	
	public void beginUpdate() {
		_previousPotential = _currentPotential;
	}

}
