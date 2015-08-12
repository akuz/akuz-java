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
	private double _patchProbsCalcIntensity;
	private boolean _isPatchProbsCalculated;
	private Fractal[] _legs;
	
	public Fractal(
			final Layer layer,
			final double size) {

		_layer = layer;
		_size = size;
		_patchProbs = new double[layer.getDim()];
		_patchProbsCalcIntensity = Double.NaN;
		_isPatchProbsCalculated = false;
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
	
	public double getPatchProbsIntensity() {
		if (!_isPatchProbsCalculated) {
			throw new IllegalStateException(
					"Patch probs have not been " + 
					"calculated for this fractal");
		}
		if (Double.isNaN(_patchProbsCalcIntensity)) {
			throw new IllegalStateException(
					"Intensity is NAN");
		}
		return _patchProbsCalcIntensity;
	}
	
	public boolean hasLegs() {
		return _legs != null;
	}
	
	public Fractal[] getLegs() {
		return _legs;
	}

	private void createLegs(final Layer layer) {
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
			final int depth) {
		
		if (depth > layers.size()) {

			// checking here that there are enough
			// layers for the created fractal
			throw new IllegalStateException(
					"Requested fractal depth is " + depth +
					", but there are not enough layers (" + 
					layers.size() + ")");
		}

		final int currDepth = this.getDepth();
		final int nextDepth = currDepth + 1;
		
		if (nextDepth <= depth) {
			
			if (!this.hasLegs()) {
				
				// we are only ensuring the depth, 
				// so creating fractal legs only
				// if they don't already exist				
				Layer nextLayer = layers.get(nextDepth-1);
				this.createLegs(nextLayer);
			}
			
			// we are now sure that this has legs
			final Fractal[] legs = this.getLegs();
			for (int i=0; i<legs.length; i++) {
				legs[i].ensureDepth(layers, depth);
			}
		}
	}
	
	public void calculatePatchProbs(
			final Image image,
			final double centerX,
			final double centerY,
			final int maxDepth) {
		
		// check current depth
		final int depth = _layer.getDepth();
		if (depth > maxDepth) {
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
			
			final double quatroSize = _size / 4.0;
			_legs[0].calculatePatchProbs(image, centerX - quatroSize, centerY - quatroSize, maxDepth);
			_legs[1].calculatePatchProbs(image, centerX + quatroSize, centerY - quatroSize, maxDepth);
			_legs[2].calculatePatchProbs(image, centerX - quatroSize, centerY + quatroSize, maxDepth);
			_legs[3].calculatePatchProbs(image, centerX + quatroSize, centerY + quatroSize, maxDepth);
			
			// average intensity from legs
			if (Double.isNaN(_patchProbsCalcIntensity)) {
				_patchProbsCalcIntensity = 0.0;
				for (int k=0; k<_legs.length; k++) {
					_patchProbsCalcIntensity += _legs[k].getPatchProbsIntensity();
				}
				_patchProbsCalcIntensity /= _legs.length;
			}
		}
	
		// calculate intensity
		if (Double.isNaN(_patchProbsCalcIntensity)) {

			// NOTE: this relies on the fact there is no Fractal jiggling
			_patchProbsCalcIntensity = image.getIntensity(centerX, centerY, _size);
		}

		// calculate each patch log like
		final DirDist layerPatchDist = _layer.getPatchDist();
		final double[] patchProbs = layerPatchDist.getPosteriorMean();
		final Patch[] patches = _layer.getPatches();
		
		// reset current values
		Arrays.fill(_patchProbs, 0.0);
		
		for (int i=0; i<patches.length; i++) {
			
			final Patch patch = patches[i];
			
			_patchProbs[i] += Math.log(patchProbs[i]);
			
			_patchProbs[i] += Math.log(patch.getIntensityDist().getProb(_patchProbsCalcIntensity));
			
			if (depth < maxDepth) {
				
				// have already checked that legs exist
				// and we have the right number of them
				
				final DirDist[] patchLegPatchDists = patch.getLegPatchDists();
				if (patchLegPatchDists == null) {
					throw new InternalError(
							"Patch doesn't have legs, " + 
							"but the fractal has");
				}
				
				_patchProbs[i] += patchLegPatchDists[0].getPosteriorLogProb(_legs[0].getPatchProbs());
				_patchProbs[i] += patchLegPatchDists[1].getPosteriorLogProb(_legs[1].getPatchProbs());
				_patchProbs[i] += patchLegPatchDists[2].getPosteriorLogProb(_legs[2].getPatchProbs());
				_patchProbs[i] += patchLegPatchDists[3].getPosteriorLogProb(_legs[3].getPatchProbs());
			}
		}
		
		StatsUtils.logLikesToProbsReplace(_patchProbs);
		_isPatchProbsCalculated = true;
	}

	public void updatePatchProbs() {
		
		final double[] thisPatchProbs = getPatchProbs();
		final DirDist layerPatchDist = _layer.getPatchDist();
		final Patch[] layerPatches = _layer.getPatches();
		
		// observe patch probs
		layerPatchDist.addObservation(thisPatchProbs);
		
		for (int i=0; i<thisPatchProbs.length; i++) {
			
			final Patch layerPatch = layerPatches[i];
			final double thisPatchProb = thisPatchProbs[i];
			
			// observe patch intensity
			layerPatch.getIntensityDist().addObservation(_patchProbsCalcIntensity, thisPatchProb);
			
			if (this.hasLegs() || layerPatch.hasLegs()) {
				
				if (!this.hasLegs() || !layerPatch.hasLegs()) {
					throw new IllegalStateException(
							"Fractal has legs, but patch doesn't");
				}
				
				final Fractal[] thisLegs = this.getLegs();
				final DirDist[] patchLegPatchDists = layerPatch.getLegPatchDists();
				
				if (thisLegs.length != patchLegPatchDists.length) {
					throw new IllegalStateException(
							"Fractal has " + thisLegs.length +" legs, " + 
							"but patch has " + patchLegPatchDists.length);
				}
				
				for (int k=0; k<thisLegs.length; k++) {
					patchLegPatchDists[k].addObservation(
							thisLegs[k].getPatchProbs(), 
							thisPatchProb);
				}
			}
		}
		
		// hierarchy
		if (_legs != null) {
			for (int k=0; k<_legs.length; k++) {
				_legs[k].updatePatchProbs();
			}
		}
	}

}
