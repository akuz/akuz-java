package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;

/**
 * Layer of an image analysis Model, containing
 * patches used at this level of detalisation.
 */
public final class Layer {
	
	private final int _depth;
	private final List<Patch> _patches;
	private Layer _nextLayer;
	
	public Layer(final int depth, final int dim) {
		_depth = depth;
		_patches = new ArrayList<>(dim);
		for (int i=0; i<dim; i++) {
			_patches.add(new Patch());
		}
	}
	
	public int getDepth() {
		return _depth;
	}
	
	public int getDim() {
		return _patches.size();
	}

	public void onNextLayerCreated(final Layer nextLayer) {
		if (_nextLayer != null) {
			throw new IllegalStateException("This layer already has a next layer");
		}
		for (int i=0; i<_patches.size(); i++) {
			_patches.get(i).onNextLayerCreated(nextLayer);
		}
		_nextLayer = nextLayer;
	}

}
