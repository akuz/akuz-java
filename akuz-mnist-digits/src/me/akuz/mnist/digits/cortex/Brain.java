package me.akuz.mnist.digits.cortex;

import me.akuz.core.math.Randoms;

public final class Brain {

	private final Randoms _randoms;
	
	private final Layer[] _layers;
	
	private double _tickDuration = 1.0 / 30.0; // Hz
	
	private double _decayHalfLife = 0.100; // Ms
	private double _decayLambda = Math.log(2) / _decayHalfLife;
	
	private double _historyHalfLife = 0.300; // Ms
	private double _historyLambda = Math.log(2) / _historyHalfLife;

	private double _combineLowerWeight = 0.7;
	private double _combineHigherWeight = 0.3;
	
	private double _reactivationThreshold = 0.5;
	private double _reactivationProbability = 0.5;

	public Brain(
			final int retinaDim1,
			final int retinaDim2,
			final int retinaColumnHeight,
			final int layerCount,
			final int layerColumnHeight) {
		
		_randoms = new Randoms();

		_layers = new Layer[layerCount];
		for (int i=0; i<layerCount; i++) {
			
			int thisColumnHeight;
			if (i==0) {
				thisColumnHeight = retinaColumnHeight;
			} else {
				thisColumnHeight = layerColumnHeight;
			}

			int lowerColumnHeight;
			if (i==0) {
				lowerColumnHeight = 0;
			} else if (i==1) {
				lowerColumnHeight = retinaColumnHeight;
			} else {
				lowerColumnHeight = layerColumnHeight;
			}
			
			// each higher layer
			// has one more dimension
			// compared to the previous
			// layer, for the diamond
			// shape connections to
			// complete at edges
			_layers[i] = new Layer(
					retinaDim1 + i,
					retinaDim2 + i,
					thisColumnHeight,
					lowerColumnHeight);
		}
	}
	
	public Randoms getRandoms() {
		return _randoms;
	}
	
	public Layer getRetina() {
		return _layers[0];
	}
	
	public Layer[] getLayers() {
		return _layers;
	}
	
	public double getTickDuration() {
		return _tickDuration;
	}
	
	public double getDecayLambda() {
		return _decayLambda;
	}
	
	public double getHistoryLambda() {
		return _historyLambda;
	}

	public double getCombineLowerWeight() {
		return _combineLowerWeight;
	}
	
	public double getCombineHigherWeight() {
		return _combineHigherWeight;
	}
	
	public double getReactivationThreshold() {
		return _reactivationThreshold;
	}
	
	public double getReactivationProbability() {
		return _reactivationProbability;
	}
	
	public void tick() {

		for (int i=0; i<_layers.length; i++) {
			_layers[i].beforeUpdate();
		}

		// don't update layer 0, because 
		// it is an input layer (retina):
		for (int i=1; i<_layers.length; i++) {
			final Layer lowerLayer = (i > 0) ? _layers[i-1] : null;
			final Layer higherLayer = (i < _layers.length-1) ? _layers[i+1] : null;
			_layers[i].update(this, lowerLayer, higherLayer);
		}
		
		for (int i=0; i<_layers.length; i++) {
			_layers[i].afterUpdate(this);
		}
	}

}
