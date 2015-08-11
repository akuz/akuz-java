package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Image analysis model, containing layers for 
 * analyzing the images at different levels of 
 * detalisation.
 */
public final class Model {
	
	private final List<Layer> _layers;
	
	public Model() {
		_layers = new ArrayList<>();
	}
	
	public List<Layer> getLayers() {
		return _layers;
	}
	
	public Layer getFirstLayer() {
		if (_layers.size() == 0) {
			throw new IllegalStateException("There are no layers in the model");
		}
		return _layers.get(0);
	}
	
	public void createNextLayer(final Random rnd, final int dim) {
		
		// FIXME: remove in favor of below function
		
		final Layer nextLayer = new Layer(rnd, _layers.size(), dim);
		if (_layers.size() > 0) {
			_layers.get(_layers.size()-1).onNextLayerCreated(nextLayer);
		}
		_layers.add(nextLayer);
	}
	
	public void ensureDepth(
			final Random rnd, 
			final int[] dims,
			final int depth) {
		
		// TODO: implement to replace the above function
		
		final Layer nextLayer = new Layer(rnd, _layers.size(), dim);
		if (_layers.size() > 0) {
			_layers.get(_layers.size()-1).onNextLayerCreated(nextLayer);
		}
		_layers.add(nextLayer);
	}
	
	public void normalize() {
		for (int i=0; i<_layers.size(); i++) {
			_layers.get(i).normalize();
		}
	}
	
	public void reset() {
		for (int i=0; i<_layers.size(); i++) {
			_layers.get(i).reset();
		}
	}
	
}
