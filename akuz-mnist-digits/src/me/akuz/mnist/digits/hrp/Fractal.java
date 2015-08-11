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
	private boolean _isPatchProbsCalculated;
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
		if (!_isPatchProbsCalculated) {
			throw new IllegalStateException(
					"Patch probs have not been " + 
					"calculated for this fractal");
		}
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

	public void ensureDepth(
			final List<Layer> layers,
			final int maxDepth) {

		final int depth = this.getDepth();
		
		if (depth >= layers.size()) {

			// checking here that there are enough
			// layers for the created fractal, in
			// particular important for layer 0
			throw new IllegalStateException(
					"Created fractal depth is " + depth +
					", but there are not enough layers (" + 
					layers.size() + ")");
		}
		
		final int nextDepth = depth + 1;
		
		if (nextDepth <= maxDepth) {
			
			if (nextDepth >= layers.size()) {
				
				// checking here that maxDepth always
				// fits the number of layers, to avoid
				// situations when caller expects there
				// to be enough layers in the model
				throw new IllegalStateException(
						"Not enough layers (" + layers.size() + 
						") to support depth " + nextDepth);
			}
			
			if (!this.hasLegs()) {
				
				// we are only ensuring the depth, 
				// so creating fractal legs only
				// if they don't already exist				
				Layer nextLayer = layers.get(nextDepth);
				this.createLegs(nextLayer);
			}
			
			// we are now sure that this has legs
			final Fractal[] legs = this.getLegs();
			for (int i=0; i<legs.length; i++) {
				legs[i].ensureDepth(layers, maxDepth);
			}
		}
	}
	
	public void calcPatchProbs(
			final Image image,
			final double centerX,
			final double centerY,
			final int maxDepth) {
		
		// check current depth
		final int depth = _layer.getDepth();
		if (maxDepth < depth) {
			throw new IllegalArgumentException(
					"Encountered the fractal with depth " + depth +
					" while calculating patch probs with maxDepth " + 
					maxDepth + ", which is not allowed");
		}
		
		// calculate leg patch probs first,
		// because they are assumed to be 
		// independent from the patches
		if (depth < maxDepth) {
			
			if (!this.hasLegs()) {
				
				throw new IllegalStateException(
						"Requested maxDepth " + maxDepth + 
						" while calculating patch probs, " +
						"but the fractal at depth " + depth +
						" doesn't have legs, which must " +
						"already exist for consistency");
			}
			
			if (_legs.length != 4) {
				throw new InternalError(
						"Expected fractal to have 4 legs " + 
						"exactly, but got " + _legs.length);
			}
			
			_legs[0].calcPatchProbs(image, centerX - _size / 2.0, centerY - _size / 2.0, maxDepth);
			_legs[1].calcPatchProbs(image, centerX + _size / 2.0, centerY - _size / 2.0, maxDepth);
			_legs[2].calcPatchProbs(image, centerX - _size / 2.0, centerY + _size / 2.0, maxDepth);
			_legs[3].calcPatchProbs(image, centerX + _size / 2.0, centerY + _size / 2.0, maxDepth);
		}
	
		// covered intensity
		final double intensity = image.getIntensity(centerX, centerY, _size);

		// calculate each patch log like
		final DirDist layerPatchDist = _layer.getPatchDist();
		final double[] patchProbs = layerPatchDist.getPosteriorMean();
		final Patch[] patches = _layer.getPatches();
		
		// reset current values
		Arrays.fill(_patchProbs, 0.0);
		
		for (int i=0; i<patches.length; i++) {
			
			final Patch patch = patches[i];
			
			_patchProbs[i] += Math.log(patchProbs[i]);
			
			_patchProbs[i] += Math.log(patch.getIntensityDist().getProb(intensity));
			
			if (depth < maxDepth) {
				
				// have already checked that legs exist
				// and we have the right number of them
				
				final DirDist[] legsPatchDist = patch.getLegsPatchDist();
				if (legsPatchDist == null) {
					throw new InternalError(
							"Patch doesn't have legsPatchDist, " + 
							"while the fractal already has legs");
				}
				
				_patchProbs[i] += legsPatchDist[0].getPosteriorLogProb(_legs[0].getPatchProbs());
				_patchProbs[i] += legsPatchDist[1].getPosteriorLogProb(_legs[1].getPatchProbs());
				_patchProbs[i] += legsPatchDist[2].getPosteriorLogProb(_legs[2].getPatchProbs());
				_patchProbs[i] += legsPatchDist[3].getPosteriorLogProb(_legs[3].getPatchProbs());
			}
		}
		
		StatsUtils.logLikesToProbsReplace(_patchProbs);
		_isPatchProbsCalculated = true;
	}

}
