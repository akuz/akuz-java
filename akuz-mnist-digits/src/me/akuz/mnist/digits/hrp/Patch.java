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
	public static final double INTENSITY_PRIOR_MEAN_SAMPLES = 30.0;
	public static final double INTENSITY_PRIOR_VARIANCE = Math.pow(0.5, 2);
	public static final double INTENSITY_PRIOR_VARIANCE_SAMPLES = 30.0;
	public static final double LEG_DIR_ALPHA_TOTAL = 10.0;

	private final NIGDist _intensityDist;
	private DirDist[] _legsPatchDist;

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
		return _legsPatchDist != null;
	}

	public DirDist[] getLegsPatchDist() {
		return _legsPatchDist;
	}

	public void onNextLayerCreated(final Layer nextLayer) {
		if (_legsPatchDist != null) {
			throw new IllegalStateException("This patch already has next layer");
		}
		final int nextDim = nextLayer.getDim();
		_legsPatchDist = new DirDist[4];
		for (int i=0; i<_legsPatchDist.length; i++) {
			_legsPatchDist[i] = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
			_legsPatchDist[i].normalize();
		}
	}
	
	public void normalize() {
		// no need to normalize intensity
		if (_legsPatchDist != null) {
			for (int i=0; i<_legsPatchDist.length; i++) {
				_legsPatchDist[i].normalize();
			}
		}
	}
	
	public void reset() {
		_intensityDist.reset();
		if (_legsPatchDist != null) {
			for (int i=0; i<_legsPatchDist.length; i++) {
				_legsPatchDist[i].reset();
			}
		}
	}

	public void print() {
		System.out.print("  Intensity: ");
		System.out.println(_intensityDist);
		if (_legsPatchDist != null) {
			for (int i=0; i<_legsPatchDist.length; i++) {
				System.out.print("  Leg " + (i+1) + ": ");
				System.out.println(_legsPatchDist[i]);
			}
		}
	}

}
