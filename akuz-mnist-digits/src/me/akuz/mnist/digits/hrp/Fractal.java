package me.akuz.mnist.digits.hrp;

import java.util.Arrays;
import java.util.List;

/**
 * Image-specific analysis fractal.
 */
public final class Fractal {
	
	private final Layer _layer;
	private final double _size;
	private final double[] _patchProbs;

	private Fractal _leg1;
	private Fractal _leg2;
	private Fractal _leg3;
	private Fractal _leg4;
	
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
		return _leg1 != null;
	}
	
	public Fractal getLeg1() {
		return _leg1;
	}
	
	public Fractal getLeg2() {
		return _leg2;
	}
	
	public Fractal getLeg3() {
		return _leg3;
	}
	
	public Fractal getLeg4() {
		return _leg4;
	}

	public void createLegs(final Layer layer) {
		final double nextSize = _size / 2.0;
		_leg1 = new Fractal(layer, nextSize);
		_leg2 = new Fractal(layer, nextSize);
		_leg3 = new Fractal(layer, nextSize);
		_leg4 = new Fractal(layer, nextSize);
	}
	
	public void calculatePatchProbs(
			final Image image,
			final double centerX,
			final double centerY) {
		
		// reset current values
		Arrays.fill(_patchProbs, 0.0);
		
		// calculate leg patch probs first,
		// because they are assumed independent
		// from the above layer patches
		if (_leg1 != null) {
			_leg1.calculatePatchProbs(image, centerX - _size / 2.0, centerY - _size / 2.0);
			_leg2.calculatePatchProbs(image, centerX + _size / 2.0, centerY - _size / 2.0);
			_leg3.calculatePatchProbs(image, centerX - _size / 2.0, centerY + _size / 2.0);
			_leg4.calculatePatchProbs(image, centerX + _size / 2.0, centerY + _size / 2.0);
		}
	
		// calculate average covered intensity
		double intensity = 0.5; // TODO
		
		// calculate each patch log like
		final double[] patchProbs = _layer.getPatchProbs();
		final Patch[] patches = _layer.getPatches();
		for (int i=0; i<patches.length; i++) {
			
			final Patch patch = patches[i];
			
			_patchProbs[i] += Math.log(patchProbs[i]); // FIXME: move in log
			
			_patchProbs[i] += patch.getIntensityLogProb(intensity);
			
			if (_leg1 != null) {
				_patchProbs[i] += patch.getLeg1LogProb(_leg1.getPatchProbs());
				_patchProbs[i] += patch.getLeg2LogProb(_leg1.getPatchProbs());
				_patchProbs[i] += patch.getLeg3LogProb(_leg1.getPatchProbs());
				_patchProbs[i] += patch.getLeg4LogProb(_leg1.getPatchProbs());
			}
		}
	}

	public static void createFractalHierarchy(
			final List<Layer> layers,
			final Fractal parent,
			final int maxDepth) {

		final int nextDepth = parent.getDepth() + 1;
		
		if (nextDepth <= maxDepth &&
			nextDepth < layers.size()) {
			
			Layer nextLayer = layers.get(nextDepth);
			parent.createLegs(nextLayer);
			
			createFractalHierarchy(layers, parent.getLeg1(), maxDepth);
			createFractalHierarchy(layers, parent.getLeg2(), maxDepth);
			createFractalHierarchy(layers, parent.getLeg3(), maxDepth);
			createFractalHierarchy(layers, parent.getLeg4(), maxDepth);	
		}
	}

}
