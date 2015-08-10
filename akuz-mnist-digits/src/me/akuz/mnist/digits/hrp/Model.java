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
	
	public void createNextLayer(final int dim) {
		final Layer nextLayer = new Layer(_layers.size(), dim);
		if (_layers.size() > 0) {
			_layers.get(_layers.size()-1).onNextLayerCreated(nextLayer);
		}
		_layers.add(nextLayer);
	}
	
	public Fractal analyze(final Image image, final int maxDepth) {
		
		if (_layers.size() == 0) {
			throw new IllegalStateException("There are no layers in the model");
		}
		if (maxDepth < 0) {
			throw new IllegalArgumentException("Argument maxDepth must be >= 0");
		}

		Fractal fractal = new Fractal(_layers.get(0), image.getMinSize());
		
		Fractal.createFractalHierarchy(_layers, fractal, maxDepth);
		
		fractal.calculatePatchProbs(image, image.getCenterX(), image.getCenterY());
		
		return fractal;
	}
	
}
