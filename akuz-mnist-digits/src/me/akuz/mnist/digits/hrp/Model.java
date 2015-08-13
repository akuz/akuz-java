package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;

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
	
	public int getDepth() {
		return _layers.size();
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
	
	public Layer getLastLayer() {
		if (_layers.size() == 0) {
			throw new IllegalStateException("There are no layers in the model");
		}
		return _layers.get(_layers.size() - 1);
	}
	
	public Layer addLayer(final LayerConfig layerConfig) {

		final Layer nextLayer = new Layer(
				_layers.size() + 1, 
				layerConfig.getSpread(),
				layerConfig.getDim());

		if (_layers.size() > 0) {
			_layers.get(_layers.size()-1).onNextLayerCreated(nextLayer);
		}
		
		_layers.add(nextLayer);
		return nextLayer;
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
	
	public void print() {
		for (int i=_layers.size()-1; i>=0; i--) {
			System.out.println("---------------------------");
			System.out.println("----- layer " + (i + 1));
			System.out.println("---------------------------");
			_layers.get(i).print();
		}
	}
	
}
