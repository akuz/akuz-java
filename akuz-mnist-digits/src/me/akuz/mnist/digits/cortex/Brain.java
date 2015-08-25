package me.akuz.mnist.digits.cortex;

public final class Brain {
	
	private final Layer[] _layers;
	
	public Brain(
			final int retinaDim1,
			final int retinaDim2,
			final int layerCount,
			final int neuronsPerColumn) {
		
		_layers = new Layer[layerCount];
		for (int i=0; i<layerCount; i++) {
			
			// each subsequent layer
			// has one more dimension
			// compared to the previous
			// layer, for the diamond
			// shape connections to
			// complete at edges:
			_layers[i] = new Layer(
					retinaDim1 + i,
					retinaDim2 + i,
					neuronsPerColumn);
		}
	}
	
	public Layer getRetina() {
		return _layers[0];
	}
	
	public void preUpdate() {
		for (int i=0; i<_layers.length; i++) {
			_layers[i].preUpdate();
		}
	}
	
	public void update() {
		for (int i=1; i<_layers.length; i++) {
			_layers[i].update(_layers[i-1]);
		}
	}

}
