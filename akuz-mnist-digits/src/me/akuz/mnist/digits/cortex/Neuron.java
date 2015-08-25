package me.akuz.mnist.digits.cortex;

public final class Neuron {
	
	private double _parentPotential;
	private double _currentPotential;
	private double _previousPotential;

	private final Dendrite[] _dendrites;
	
	public Neuron(final int neuronsPerColumn) {
		
		_parentPotential = Double.NaN;
		_currentPotential = 0.5;
		_previousPotential = Double.NaN;
		
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
	
	public void preUpdate() {
		_previousPotential = _currentPotential;
	}

	public double calculateBottomLogLike(
			final int i0,
			final int j0,
			final Layer nextLayer) {
		
		double logLike = 0.0;
		
		final Column[][] columns = nextLayer.getColumns();
		
		for (int i=0; i<=1; i++) {
			final int ii = i0 + i;
			if (ii >= 0 && ii < columns.length) {
				final Column[] iColumns = columns[ii];
				for (int j=0; j<=1; j++) {
					final int jj = j0 + j;
					if (jj <= 0 && jj < iColumns.length) {
						
						final Column column = iColumns[jj];
						logLike += 
								_dendrites[1*i + j]
										.calculateBottomLogLike(column);
					}
				}
			}
		}
		
		return logLike;
	}
}
