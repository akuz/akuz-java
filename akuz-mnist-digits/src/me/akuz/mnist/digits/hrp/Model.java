package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;

/**
 * Image analysis model, containing layers for 
 * analyzing the images at different depths.
 */
public final class Model {
	
	private final List<Layer> _layers;
	
	public Model() {
		_layers = new ArrayList<>();
	}
	
	public void createNextLayer(final int dim) {
		final Layer nextLayer = new Layer(_layers.size(), dim);
		if (_layers.size() > 0) {
			_layers.get(_layers.size()-1).onNextLayerCreated(nextLayer);
		}
		_layers.add(nextLayer);
	}
	
}
