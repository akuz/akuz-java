package me.akuz.mnist.digits.hrp;

import me.akuz.core.math.DirDist;

/**
 * Layer of an image analysis Model, containing
 * patches used at this level of detalisation.
 */
public final class Layer {

	// we want the probabilities of patches within layer
	// to be as equal as possible, therefore we need to use
	// a high value for the dirichlet alpha (perhaps could
	// remove this distribution completely and assume flat)
	public static final double PATCH_DIR_ALPHA_TOTAL = 1000000.0;

	private final int _depth;
	private final Spread _spread;
	private final DirDist _patchDist;
	private final Patch[] _patches;
	private Layer _nextLayer;
	
	public Layer(final int depth, final Spread spread, final int dim) {
		_depth = depth;
		_spread = spread;
		_patchDist = new DirDist(dim, PATCH_DIR_ALPHA_TOTAL / dim);
		_patches = new Patch[dim];
		for (int i=0; i<dim; i++) {
			_patches[i] = new Patch(this, 1.0 - (double)i/(double)(dim - 1));
		}
		normalize();
	}
	
	public int getDepth() {
		return _depth;
	}
	
	public Spread getSpread() {
		return _spread;
	}
	
	public Layer getNextLayer() {
		return _nextLayer;
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

	public void print() {
		System.out.print("Patch Dist: ");
		System.out.println(_patchDist);
		for (int i=0; i<_patches.length; i++) {
			System.out.println("Patch " + (i+1));
			_patches[i].print();
		}
	}

}
