package me.akuz.mnist.digits.cortex;

import java.util.concurrent.ThreadLocalRandom;

public final class Neuron {
	
	private double _currentPotential;
	private double _previousPotential;
	private double _historicalPotential;

	private final Dendrite[] _dendrites;
	
	public Neuron(final Brain brain, final int lowerColumnHeight) {
		
		_currentPotential = 0.1 + 0.8 * ThreadLocalRandom.current().nextDouble();
		_previousPotential = Double.NaN;
		_historicalPotential = 0.0;
		
		if (lowerColumnHeight > 0) {
			_dendrites = new Dendrite[4];
			for (int i=0; i<_dendrites.length; i++) {
				_dendrites[i] = new Dendrite(brain, lowerColumnHeight);
			}
		} else {
			_dendrites = null;
		}
	}
	
	public double getCurrentPotential() {
		return _currentPotential;
	}
	
	public void setCurrentPotential(final Brain brain, final double value) {
		_currentPotential = value;
	}
	
	public double getHistoricalPotential() {
		return _historicalPotential;
	}
	
	public void updateHistoricalPotential(final Brain brain) {
		final double decay = Math.exp(-brain.getHistoryLambda() * brain.getTickDuration());
		_historicalPotential = decay * _historicalPotential + (1.0 - decay) * _currentPotential;
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

	public void learnTick(
			Brain brain,
			int i0,
			int j0,
			Layer lowerLayer) {

		if (_dendrites == null) {
			throw new IllegalStateException(
					"Neuron does not have dendrites, " +
					"cannot update dendrite weights");
		}
		
		int lowerColumnCount = 0;
		
		final Column[][] lowerColumns = lowerLayer.getColumns();

		final double learnWeightNow = _currentPotential * brain.getLearnWeightPerTick();
		
		for (int i=-1; i<=0; i++) {
			final int ii = i0 + i;
			if (ii >= 0 && ii < lowerColumns.length) {
				final Column[] iiLowerColumns = lowerColumns[ii];
				for (int j=-1; j<=0; j++) {
					final int jj = j0 + j;
					if (jj >= 0 && jj < iiLowerColumns.length) {
						
						final Column lowerColumn = iiLowerColumns[jj];
						final Dendrite dendrite = _dendrites[(i+1)*2 + (j+1)];

						dendrite.learnTick(lowerColumn, learnWeightNow);
						
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
		
	}
}
