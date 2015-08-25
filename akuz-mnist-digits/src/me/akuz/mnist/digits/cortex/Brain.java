package me.akuz.mnist.digits.cortex;

public final class Brain {

	private final Layer[] _layers;
	
	private double _tickDuration = 1.0 / 30.0; // Frequency 30 Hz
	private double _decayLambda = Math.log(2) / 0.1; // Half-life 100 Ms

	// below, dividing each weight by the total of all weights
	private double _combineTimeWeight = 4.0 / 5.0; // would take 4 time decays to go down to 1
	private double _combineLowerWeight = 0.67 / 5.0; // prioritize lower information flow over higher
	private double _combineHigherWeight = 0.33 / 5.0; // higher information is 2 time less important than lower
	
	private double _randomActivationThreshold = 1.1; // multiplier of the probability of neuron in a flat distribution

	public Brain(
			final int retinaDim1,
			final int retinaDim2,
			final int layerCount,
			final int columnHeight) {

		_layers = new Layer[layerCount];
		for (int i=0; i<layerCount; i++) {
			
			final int thisColumnHeight = columnHeight;
			final int lowerColumnHeight = i > 0 ? columnHeight : 0;
			
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
	
	public double getCombineTimeWeight() {
		return _combineTimeWeight;
	}
	
	public double getCombineLowerWeight() {
		return _combineLowerWeight;
	}
	
	public double getCombineHigherWeight() {
		return _combineHigherWeight;
	}
	
	public double getRandomActivationThreshold() {
		return _randomActivationThreshold;
	}
	
	public void tick() {

		// don't update layer 0,
		// because it is an input
		// layer (retina)

		for (int i=1; i<_layers.length; i++) {
			_layers[i].beforeUpdate();
		}

		for (int i=1; i<_layers.length; i++) {
			final Layer lowerLayer = _layers[i-1];
			final Layer higherLayer = (i < _layers.length-1) ? _layers[i+1] : null;
			_layers[i].update(this, lowerLayer, higherLayer);
		}
		
		for (int i=1; i<_layers.length; i++) {
			_layers[i].afterUpdate(this);
		}
	}

}
