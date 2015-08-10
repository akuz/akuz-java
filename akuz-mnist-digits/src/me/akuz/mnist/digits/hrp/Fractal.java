package me.akuz.mnist.digits.hrp;

import java.util.Arrays;
import java.util.List;

import me.akuz.core.math.DirDist;
import me.akuz.core.math.StatsUtils;

/**
 * Image-specific analysis fractal.
 */
public final class Fractal {
	
	private final Layer _layer;
	private final double _size;
	private final double[] _patchProbs;
	private Fractal[] _legs;
	
	public Fractal(final Layer layer, final double size) {
		_layer = layer;
		_size = size;
		_patchProbs = new double[layer.getDim()];
	}
	
	public int getDepth() {
		return _layer.getDepth();
	}

	public double getSize() {
		return _size;
	}
	
	public double[] getPatchProbs() {
		return _patchProbs;
	}
	
	public boolean hasLegs() {
		return _legs != null;
	}
	
	public Fractal[] getLegs() {
		return _legs;
	}

	public void createLegs(final Layer layer) {
		if (_legs != null) {
			throw new IllegalStateException("This fractal already has legs");
		}
		final double nextSize = _size / 2.0;
		_legs = new Fractal[4];
		for (int i=0; i<_legs.length; i++) {
			_legs[i] = new Fractal(layer, nextSize);
		}
	}
	
	public void calculatePatchProbs(
			final Image image,
			final double centerX,
			final double centerY,
			final int maxDepth) {
		
		final int depth = _layer.getDepth();
		if (maxDepth >= 0 && depth > maxDepth) {
			throw new IllegalArgumentException(
					"Max depth is " + maxDepth + 
					", but this fractal depth is " + depth + 
					", so cannot calculate patch probs");
		}
		
		// reset current values
		Arrays.fill(_patchProbs, 0.0);
		
		// calculate leg patch probs first,
		// because they are assumed independent
		// from the above layer patches
		if (_legs != null && _layer.hasNextLayer() && (maxDepth < 0 || depth < maxDepth)) {
			_legs[0].calculatePatchProbs(image, centerX - _size / 2.0, centerY - _size / 2.0, maxDepth);
			_legs[1].calculatePatchProbs(image, centerX + _size / 2.0, centerY - _size / 2.0, maxDepth);
			_legs[2].calculatePatchProbs(image, centerX - _size / 2.0, centerY + _size / 2.0, maxDepth);
			_legs[3].calculatePatchProbs(image, centerX + _size / 2.0, centerY + _size / 2.0, maxDepth);
		}
	
		// calculate covered intensity
		final double intensity = image.getIntensity(centerX, centerY, _size);

		// calculate each patch log like
		final DirDist layerPatchDist = _layer.getPatchDist();
		final double[] patchProbs = layerPatchDist.getPosteriorMean();
		final Patch[] patches = _layer.getPatches();
		for (int i=0; i<patches.length; i++) {
			
			final Patch patch = patches[i];
			
			_patchProbs[i] += Math.log(patchProbs[i]);
			
			_patchProbs[i] += Math.log(patch.getIntensityDist().getProb(intensity));
			
			if (_legs != null && _layer.hasNextLayer() && (maxDepth < 0 || depth < maxDepth)) {
				
				final DirDist[] legsPatchDist = patch.getLegsPatchDist();
				if (legsPatchDist == null) {
					throw new IllegalStateException("Layer has next layer, but patch doesn't have legs patch dist");
				}
				
				_patchProbs[i] += legsPatchDist[0].getPosteriorLogProb(_legs[0].getPatchProbs());
				_patchProbs[i] += legsPatchDist[1].getPosteriorLogProb(_legs[1].getPatchProbs());
				_patchProbs[i] += legsPatchDist[2].getPosteriorLogProb(_legs[2].getPatchProbs());
				_patchProbs[i] += legsPatchDist[3].getPosteriorLogProb(_legs[3].getPatchProbs());
			}
		}
		
		StatsUtils.logLikesToProbsReplace(_patchProbs);
	}

	public static void createHierarchy(
			final List<Layer> layers,
			final Fractal parent,
			final int maxDepth) {

		final int nextDepth = parent.getDepth() + 1;
		
		if (nextDepth <= maxDepth &&
			nextDepth < layers.size()) {
			
			Layer nextLayer = layers.get(nextDepth);
			parent.createLegs(nextLayer);
			
			final Fractal[] legs = parent.getLegs();
			
			createHierarchy(layers, legs[0], maxDepth);
			createHierarchy(layers, legs[1], maxDepth);
			createHierarchy(layers, legs[2], maxDepth);
			createHierarchy(layers, legs[3], maxDepth);	
		}
	}

}
