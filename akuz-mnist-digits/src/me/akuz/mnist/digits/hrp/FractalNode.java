package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.akuz.core.math.DirDist;
import me.akuz.core.math.StatsUtils;

public final class FractalNode {
	
	private final Layer _layer;
	private FractalLink[][] _parents;
	private FractalLink[][] _children;
	private double _patchProbsIntensity;
	private double[] _patchProbs;
	
	public FractalNode(final Layer layer) {

		_layer = layer;
		_patchProbsIntensity = Double.NaN;
	}
	
	public Layer getLayer() {
		return _layer;
	}

	public int getDepth() {
		return _layer.getDepth();
	}
	
	public double[] getPatchProbs() {
		return _patchProbs;
	}
	
	public double getPatchProbsColor() {
		if (_patchProbs == null) {
			throw new IllegalStateException(
					"Patch probs have not been " + 
					"calculated for this fractal, " + 
					"so there is no color yet");
		}
		if (Double.isNaN(_patchProbsIntensity)) {
			throw new IllegalStateException(
					"Color is NAN");
		}
		return _patchProbsIntensity;
	}
	
	public boolean hasChildren() {
		return _children != null;
	}
	
	public FractalLink[][] getChildren() {
		return _children;
	}
	
	public void setChild(final int k, final int l, final FractalLink link) {
		if (_children == null) {
			_children = new FractalLink[2][2];
		}
		_children[k][l] = link;
	}
	
	public boolean hasParents() {
		return _parents != null;
	}
	
	public FractalLink[][] getParents() {
		return _parents;
	}
	
	public void setParent(final int k, final int l, final FractalLink link) {
		if (_parents == null) {
			_parents = new FractalLink[2][2];
		}
		_parents[k][l] = link;
	}

	public void calculatePatchProbs(
			final Image image,
			final double centerX,
			final double centerY,
			final double size,
			final int minDepth,
			final int maxDepth,
			final double[] forcePatchProbs) {
		
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
			
			if (_children == null) {
				throw new IllegalStateException(
						"Requested maxDepth " + maxDepth + 
						" while calculating patch probs, " +
						"but the fractal at depth " + 
						depth + " doesn't have legs");
			}
			
			final Spread spread = _layer.getSpread();
			
			if (_children.length != spread.getLegCount()) {
				throw new InternalError(
						"Expected fractal to have " + 
						spread.getLegCount() +
						" legs exactly, but got " + 
						_children.length);
			}
			
			final SpreadLeg[] spreadLegs = spread.getLegs();
			
			final double halfSize = size / 2.0;
			final double thisLeftX = centerX - halfSize;
			final double thisLeftY = centerY - halfSize;

			for (int k=0; k<_children.length; k++) {
				
				final SpreadLeg spreadLeg = spreadLegs[k];
				
				final double legCenterX = thisLeftX + spreadLeg.getCenterX() * size;
				final double legCenterY = thisLeftY + spreadLeg.getCenterY() * size;
				final double legSize = spreadLeg.getSize() * size;
				
				_children[k].calculatePatchProbs(
						image,
						legCenterX,
						legCenterY,
						legSize,
						minDepth,
						maxDepth,
						null);
			}
		}

		if (depth >= minDepth) {
			
			// calculate intensity
			if (Double.isNaN(_patchProbsIntensity)) {

				// NOTE: this relies on the fact there is no Fractal jiggling
				_patchProbsIntensity = image.getColor(centerX, centerY, size);
			}

			final Patch[] patches = _layer.getPatches();
			
			if (forcePatchProbs != null) {
				
				if (patches.length != patches.length) {
					throw new IllegalArgumentException(
							"Expected forcePatchProbs to have length " + 
							patches.length + ", but got " + forcePatchProbs.length);
				}
				
				_patchProbs = forcePatchProbs;

			} else {
			
				// patch priors
				double[] patchPriorProbs;
				if (_parents != null && _parents.getLegsPatchPriors() != null) {
		
					patchPriorProbs = _parents.getLegsPatchPriors()[_parentLegIndex];
		
				} else {
		
					final DirDist layerPatchDist = _layer.getPatchDist();
					patchPriorProbs = layerPatchDist.getPosteriorMean();
				}
				
				// ensure created
				if (_patchProbs == null) {
					_patchProbs = new double[_layer.getDim()];
					
				} else {

					// reset current values
					Arrays.fill(_patchProbs, 0.0);
				}
				
				// calculate each patch log like
				for (int i=0; i<patches.length; i++) {
					
					final Patch patch = patches[i];
					
					_patchProbs[i] += Math.log(patchPriorProbs[i]);
					
					_patchProbs[i] += Math.log(patch.getIntensityDist().getProb(_patchProbsIntensity));
					
					if (_children != null) {
						
						final DirDist[] patchLegPatchDists = patch.getChildrenPatchDists();
	
						if (patchLegPatchDists == null) {
							throw new InternalError(
									"Patch doesn't have legs, " + 
									"but the fractal has");
						}
	
						for (int k=0; k<_children.length; k++) {
							
							_patchProbs[i] += 
								patchLegPatchDists[k]
									.getPosteriorLogProb(
										_children[k].getDownPatchProbs());
						}
					}
				}
				
				// normalize patch probs
				StatsUtils.logLikesToProbsReplace(_patchProbs);
			}
			
			// aggregate leg patch priors
			if (_children != null) {
				
				// get and check next layer
				final Layer nextLayer = _layer.getNextLayer();
				if (nextLayer == null) {
					throw new IllegalStateException(
							"Expected non-null next layer");
				}
				
				// create or reset leg priors
				if (_legsPatchPriors == null) {
					_legsPatchPriors = new double[_children.length][];
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
					final double patchProb = _patchProbs[i];
					final DirDist[] patchLegPatchDists = patch.getChildrenPatchDists();
					
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
							_legsPatchPriors[k][j] += patchProb * legPosteriorMean[j];
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

			final double[] patchProbs = getPatchProbs();
			final DirDist layerPatchDist = _layer.getPatchDist();
			final Patch[] layerPatches = _layer.getPatches();
			
			// observe patch probs
			layerPatchDist.addObservation(patchProbs);
			
			for (int i=0; i<patchProbs.length; i++) {
				
				final Patch layerPatch = layerPatches[i];
				final double patchProb = patchProbs[i];
				
				// observe patch intensity
				layerPatch.getIntensityDist().addObservation(_patchProbsIntensity, patchProb);
				
				if (this.hasChildren() || layerPatch.hasLegs()) {
					
					if (!this.hasChildren() || !layerPatch.hasLegs()) {
						throw new IllegalStateException(
								"Fractal has legs, but patch doesn't");
					}
					
					final FractalLink[][] children = this.getChildren();
					final DirDist[] patchLegPatchDists = layerPatch.getChildrenPatchDists();
					
					if (children.length != patchLegPatchDists.length) {
						throw new IllegalStateException(
								"Fractal has " + children.length +" legs, " + 
								"but patch has " + patchLegPatchDists.length);
					}
					
					for (int k=0; k<children.length; k++) {
						patchLegPatchDists[k].addObservation(
								children[k].getPatchProbs(), 
								patchProb);
					}
				}
			}			
		}

		if (depth < maxDepth) {

			if (_children == null) {
				throw new IllegalStateException(
						"Requested maxDepth " + maxDepth + 
						" while updating patch probs, " +
						"but the fractal at depth " + depth +
						" doesn't have legs, which must " +
						"already exist for consistency");
			}
			
			for (int k=0; k<_children.length; k++) {
				_children[k].updatePatchProbs(minDepth, maxDepth);
			}
		}
	}

}
