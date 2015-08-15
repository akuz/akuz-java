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
	private final Fractal _parent;
	private final int _parentLegIndex;
	private double[] _patchProbs;
	private double _patchProbsCalcIntensity;
	private boolean _isPatchProbsCalculated;
	private Fractal[] _legs;
	private double[][] _legsPatchPriors;
	
	public Fractal(
			final Layer layer, 
			final Fractal parent, 
			int parentLegIndex) {

		_layer = layer;
		_parent = parent;
		_parentLegIndex = parentLegIndex;
		_patchProbsCalcIntensity = Double.NaN;
		_isPatchProbsCalculated = false;
	}
	
	public int getDepth() {
		return _layer.getDepth();
	}
	
	public Layer getLayer() {
		return _layer;
	}
	
	public double[] getPatchProbs() {
		if (!_isPatchProbsCalculated) {
			throw new IllegalStateException(
					"Patch probs have not been " + 
					"calculated for this fractal");
		}
		return _patchProbs;
	}
	
	public double[][] getLegsPatchPriors() {
		return _legsPatchPriors;
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
			
			// ensure legs created
			if (_legs == null) {
				
				// legs spread
				final Spread spread = _layer.getSpread();
				Layer nextLayer = layers.get(nextDepth-1);
				
				// create the legs
				_legs = new Fractal[spread.getLegCount()];
				for (int k=0; k<_legs.length; k++) {
					_legs[k] = new Fractal(nextLayer, this, k);
				}
			}

			// we are now sure legs created
			for (int k=0; k<_legs.length; k++) {
				_legs[k].ensureDepth(layers, depth);
			}
		}
	}
	
	public void calculatePatchProbs(
			final Image image,
			final double centerX,
			final double centerY,
			final double size,
			final int minDepth,
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
			
			if (_legs == null) {
				throw new IllegalStateException(
						"Requested maxDepth " + maxDepth + 
						" while calculating patch probs, " +
						"but the fractal at depth " + 
						depth + " doesn't have legs");
			}
			
			final Spread spread = _layer.getSpread();
			
			if (_legs.length != spread.getLegCount()) {
				throw new InternalError(
						"Expected fractal to have " + 
						spread.getLegCount() +
						" legs exactly, but got " + 
						_legs.length);
			}
			
			final SpreadLeg[] spreadLegs = spread.getLegs();
			
			final double halfSize = size / 2.0;
			final double thisLeftX = centerX - halfSize;
			final double thisLeftY = centerY - halfSize;

			for (int k=0; k<_legs.length; k++) {
				
				final SpreadLeg spreadLeg = spreadLegs[k];
				
				final double legCenterX = thisLeftX + spreadLeg.getCenterX() * size;
				final double legCenterY = thisLeftY + spreadLeg.getCenterY() * size;
				final double legSize = spreadLeg.getSize() * size;
				
				_legs[k].calculatePatchProbs(
						image,
						legCenterX,
						legCenterY,
						legSize,
						minDepth,
						maxDepth);
			}
		}

		if (depth >= minDepth) {
			
			// calculate intensity
			if (Double.isNaN(_patchProbsCalcIntensity)) {

				// NOTE: this relies on the fact there is no Fractal jiggling
				_patchProbsCalcIntensity = image.getIntensity(centerX, centerY, size);
			}
			
			// patch priors
			double[] patchPriorProbs;
			if (_parent != null && _parent.getLegsPatchPriors() != null) {
	
				patchPriorProbs = _parent.getLegsPatchPriors()[_parentLegIndex];
	
			} else {
	
				final DirDist layerPatchDist = _layer.getPatchDist();
				patchPriorProbs = layerPatchDist.getPosteriorMean();
			}
			
			// ensure created
			if (_patchProbs == null) {
				_patchProbs = new double[_layer.getDim()];
			}
	
			// reset current values
			Arrays.fill(_patchProbs, 0.0);
			
			// calculate each patch log like
			final Patch[] patches = _layer.getPatches();
			for (int i=0; i<patches.length; i++) {
				
				final Patch patch = patches[i];
				
				_patchProbs[i] += Math.log(patchPriorProbs[i]);
				
				_patchProbs[i] += Math.log(patch.getIntensityDist().getProb(_patchProbsCalcIntensity));
				
				if (_legs != null) {
					
					final DirDist[] patchLegPatchDists = patch.getLegPatchDists();

					if (patchLegPatchDists == null) {
						throw new InternalError(
								"Patch doesn't have legs, " + 
								"but the fractal has");
					}

					for (int k=0; k<_legs.length; k++) {
						
						_patchProbs[i] += 
							patchLegPatchDists[k]
								.getPosteriorLogProb(
									_legs[k].getPatchProbs());
					}
				}
			}
			
			// normalize patch probs
			StatsUtils.logLikesToProbsReplace(_patchProbs);
			
			// aggregate leg patch priors
			if (_legs != null) {
				
				// get and check next layer
				final Layer nextLayer = _layer.getNextLayer();
				if (nextLayer == null) {
					throw new IllegalStateException(
							"Expected non-null next layer");
				}
				
				// create or reset leg priors
				if (_legsPatchPriors == null) {
					_legsPatchPriors = new double[_legs.length][];
					for (int k=0; k<_legsPatchPriors.length; k++) {
						_legsPatchPriors[k] = new double[nextLayer.getDim()];
					}
				} else {
					for (int k=0; k<_legsPatchPriors.length; k++) {
						Arrays.fill(_legsPatchPriors[k], 0);
					}
				}
				
				// aggregate leg prior probs
				for (int i=0; i<patches.length; i++) {
					
					final Patch patch = patches[i];
					final DirDist[] patchLegPatchDists = patch.getLegPatchDists();
					
					if (_legsPatchPriors.length != patchLegPatchDists.length) {
						throw new IllegalStateException(
								"Legs count " + _legsPatchPriors.length + 
								" does not match patch leg dists count " + 
								patchLegPatchDists.length);
					}
					
					for (int k=0; k<_legsPatchPriors.length; k++) {
						
						final DirDist legPatchDist = patchLegPatchDists[k];
						final double[] legPosteriorMean = legPatchDist.getPosteriorMean();
						
						for (int j=0; j<_legsPatchPriors[k].length; j++) {
							_legsPatchPriors[k][j] += _patchProbs[i] * legPosteriorMean[j];
						}
					}
				}
			}
			
			_isPatchProbsCalculated = true;
		}
	}

	public void updatePatchProbs(
			final int minDepth,
			final int maxDepth) {
		
		final int depth = _layer.getDepth();
		
		if (minDepth <= depth && depth <= maxDepth) {

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
		}

		if (depth < maxDepth) {

			if (_legs == null) {
				throw new IllegalStateException(
						"Requested maxDepth " + maxDepth + 
						" while updating patch probs, " +
						"but the fractal at depth " + depth +
						" doesn't have legs, which must " +
						"already exist for consistency");
			}
			
			for (int k=0; k<_legs.length; k++) {
				_legs[k].updatePatchProbs(minDepth, maxDepth);
			}
		}
	}

}
