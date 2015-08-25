package me.akuz.mnist.digits.cortex;

public final class Brain {

	private final Layer[] _layers;
	
	private double _timeTick = 1.0 / 30.0; // Frequency 30 Hz
	private double _timeDecay = Math.log(2) / 0.1; // Half-life 100 Ms
	
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
	
	public double getTimeTick() {
		return _timeTick;
	}
	
	public double getTimeDecay() {
		return _timeDecay;
	}
	
	public void tick() {

		// don't update layer 0,
		// because it is an input
		// layer (retina)

		for (int i=1; i<_layers.length; i++) {
			_layers[i].beforeUpdate();
		}

		for (int i=1; i<_layers.length; i++) {
			_layers[i].update(this, _layers[i-1]);
		}
		
		for (int i=1; i<_layers.length; i++) {
			_layers[i].afterUpdate(this);
		}
	}

}
