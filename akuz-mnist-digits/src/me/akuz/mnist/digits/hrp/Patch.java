package me.akuz.mnist.digits.hrp;

import java.util.Random;

import me.akuz.core.math.DirDist;
import me.akuz.core.math.NIGDist;

/**
 * Patch of which the images are built 
 * at a specific level of detalisation.
 */
public final class Patch {

	public static final double INTENSITY_PRIOR_MEAN = 0.5;
	public static final double INTENSITY_PRIOR_MEAN_NOISE = 0.1;
	public static final double INTENSITY_PRIOR_MEAN_SAMPLES = 10.0;
	public static final double INTENSITY_PRIOR_VARIANCE = Math.pow(0.2, 2);
	public static final double INTENSITY_PRIOR_VARIANCE_SAMPLES = 10.0;
	public static final double LEG_DIR_ALPHA_TOTAL = 1.0;

	private final NIGDist _intensityDist;
	private DirDist[] _legPatchDists;

	// TODO: replace with float
	public Patch(final Random rnd) {
		_intensityDist = new NIGDist(
				INTENSITY_PRIOR_MEAN + (rnd.nextDouble() - 0.5) * INTENSITY_PRIOR_MEAN_NOISE, 
				INTENSITY_PRIOR_MEAN_SAMPLES,
				INTENSITY_PRIOR_VARIANCE,
				INTENSITY_PRIOR_VARIANCE_SAMPLES);
	}
	
	public NIGDist getIntensityDist() {
		return _intensityDist;
	}
	
	public boolean hasLegs() {
		return _legPatchDists != null;
	}

	public DirDist[] getLegPatchDists() {
		return _legPatchDists;
	}

	public void onNextLayerCreated(final Layer nextLayer) {
		if (_legPatchDists != null) {
			throw new IllegalStateException("This patch already has next layer");
		}
		final int nextDim = nextLayer.getDim();
		_legPatchDists = new DirDist[4];
		for (int k=0; k<_legPatchDists.length; k++) {
			_legPatchDists[k] = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
			_legPatchDists[k].normalize();
		}
	}
	
	public void normalize() {
		// no need to normalize intensity
		if (_legPatchDists != null) {
			for (int k=0; k<_legPatchDists.length; k++) {
				_legPatchDists[k].normalize();
			}
		}
	}
	
	public void reset() {
		_intensityDist.reset();
		if (_legPatchDists != null) {
			for (int k=0; k<_legPatchDists.length; k++) {
				_legPatchDists[k].reset();
			}
		}
	}

	public void print() {
		System.out.print("  Intensity: ");
		System.out.println(_intensityDist);
		if (_legPatchDists != null) {
			for (int k=0; k<_legPatchDists.length; k++) {
				System.out.print("  Leg " + (k+1) + ": ");
				System.out.println(_legPatchDists[k]);
			}
		}
	}

}
