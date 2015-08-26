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
		int lowerColumnCount = 0;
		
		final Column[][] lowerColumns = lowerLayer.getColumns();
		
		for (int i=-1; i<=0; i++) {
			final int ii = i0 + i;
			if (ii >= 0 && ii < lowerColumns.length) {
				final Column[] iiLowerColumns = lowerColumns[ii];
				for (int j=-1; j<=0; j++) {
					final int jj = j0 + j;
					if (jj >= 0 && jj < iiLowerColumns.length) {
						
						final Column lowerColumn = iiLowerColumns[jj];
						logLike += 
								_dendrites[1*(i+1) + j+1]
										.calculateLowerLogLike(lowerColumn);
						
						lowerColumnCount++;
					}
				}
			}
		}
		
		if (lowerColumnCount == 0) {
			throw new IllegalStateException(
					"Could not find any columns in the lower " +
					"layer that could attach a dendrite to");
		}
		
		return logLike;
	}
}
