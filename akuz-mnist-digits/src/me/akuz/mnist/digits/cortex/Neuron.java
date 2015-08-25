package me.akuz.mnist.digits.cortex;

import java.util.concurrent.ThreadLocalRandom;

public final class Neuron {
	
	private double _currentPotential;
	private double _previousPotential;

	private final Dendrite[] _dendrites;
	
	public Neuron(final int lowerColumnHeight) {
		
		_currentPotential = ThreadLocalRandom.current().nextDouble();
		_previousPotential = Double.NaN;
		
		if (lowerColumnHeight > 0) {
			_dendrites = new Dendrite[4];
			for (int i=0; i<_dendrites.length; i++) {
				_dendrites[i] = new Dendrite(lowerColumnHeight);
			}
		} else {
			_dendrites = null;
		}
	}
	
	public double getCurrentPotential() {
		return _currentPotential;
	}
	
	public void setCurrentPotential(double value) {
		_currentPotential = value;
	}
	
	public double getPreviousPotential() {
		return _previousPotential;
	}
	
	public Dendrite[] getDendrites() {
		return _dendrites;
	}
	
	public void beforeUpdate() {
		_previousPotential = _currentPotential;
	}

	public double calculateLowerLogLike(
			final int i0,
			final int j0,
			final Layer lowerLayer) {
		
		if (_dendrites == null) {
			throw new IllegalStateException(
					"Neuron does not have dendrites, " +
					"cannot calculate bottom log like");
		}
		
		double logLike = 0.0;
		
		final Column[][] columns = lowerLayer.getColumns();
		
		for (int i=0; i<=1; i++) {
			final int ii = i0 + i;
			if (ii >= 0 && ii < columns.length) {
				final Column[] iColumns = columns[ii];
				for (int j=0; j<=1; j++) {
					final int jj = j0 + j;
					if (jj >= 0 && jj < iColumns.length) {
						
						final Column column = iColumns[jj];
						logLike += 
								_dendrites[1*i + j]
										.calculateLowerLogLike(column);
					}
				}
			}
		}
		
		return logLike;
	}
}
