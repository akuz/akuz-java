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
	
	public void createNextLayer(final Random rnd, final int dim) {
		final Layer nextLayer = new Layer(rnd, _layers.size(), dim);
		if (_layers.size() > 0) {
			_layers.get(_layers.size()-1).onNextLayerCreated(nextLayer);
		}
		_layers.add(nextLayer);
	}
	
	public Fractal createRootFractal(final Image image) {
		if (_layers.size() == 0) {
			throw new IllegalStateException("There are no layers in the model");
		}
		return new Fractal(_layers.get(0), image.getMinSize());
	}
	
	public Fractal calculatePatchProbs(final Image image, final int maxDepth) {

		if (_layers.size() == 0) {
			throw new IllegalStateException("There are no layers in the model");
		}
		
		// TODO: extend fractal
		
		// TODO: throw if model not deep enough
		
		// TODO: consider throwing in other methods with maxDepth
		
		Fractal fractal = new Fractal(_layers.get(0), image.getMinSize());
		
		Fractal.createHierarchy(_layers, fractal, maxDepth);
		
		fractal.calculatePatchProbs(image, image.getCenterX(), image.getCenterY(), maxDepth);
		
		return fractal;
	}
	
}
