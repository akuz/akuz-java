package me.akuz.mnist.digits.hrp;

import java.util.Random;

import me.akuz.core.math.DirDist;

/**
 * Layer of an image analysis Model, containing
 * patches used at this level of detalisation.
 */
public final class Layer {

	public static final double PATCH_DIR_ALPHA_TOTAL = 10.0;
	
	private final int _depth;
	private final DirDist _patchDist;
	private final Patch[] _patches;
	private Layer _nextLayer;
	
	public Layer(final Random rnd, final int depth, final int dim) {
		_depth = depth;
		_patchDist = new DirDist(dim, PATCH_DIR_ALPHA_TOTAL / dim);
		_patches = new Patch[dim];
		for (int i=0; i<dim; i++) {
			_patches[i] = new Patch(rnd);
		}
	}
	
	public int getDepth() {
		return _depth;
	}
	
	public int getDim() {
		return _patches.length;
	}
	
	public DirDist getPatchDist() {
		return _patchDist;
	}
	
	public Patch[] getPatches() {
		return _patches;
	}
	
	public boolean hasNextLayer() {
		return _nextLayer != null;
	}

	public void onNextLayerCreated(final Layer nextLayer) {
		if (_nextLayer != null) {
			throw new IllegalStateException("This layer already has a next layer");
		}
		for (int i=0; i<_patches.length; i++) {
			_patches[i].onNextLayerCreated(nextLayer);
		}
		_nextLayer = nextLayer;
	}
	
	public void normalize() {
		_patchDist.normalize();
		for (int i=0; i<_patches.length; i++) {
			_patches[i].normalize();
		}
	}
	
	public void reset() {
		_patchDist.reset();
		for (int i=0; i<_patches.length; i++) {
			_patches[i].reset();
		}
	}

}
