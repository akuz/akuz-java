package me.akuz.mnist.digits.hrp;

/**
 * Layer of an image analysis Model, containing
 * patches used at this level of detalisation.
 */
public final class Layer {
	
	private final int _depth;
	private final double[] _patchProbs;
	private final Patch[] _patches;
	private Layer _nextLayer;
	
	public Layer(final int depth, final int dim) {
		_depth = depth;
		_patchProbs = new double[dim];
		_patches = new Patch[dim];
		for (int i=0; i<dim; i++) {
			_patches[i] = new Patch();
		}
	}
	
	public int getDepth() {
		return _depth;
	}
	
	public int getDim() {
		return _patches.length;
	}
	
	public double[] getPatchProbs() {
		return _patchProbs;
	}
	
	public Patch[] getPatches() {
		return _patches;
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

}
