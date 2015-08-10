package me.akuz.mnist.digits.hrp;

import me.akuz.core.math.DirDist;
import me.akuz.core.math.NIGDist;

/**
 * Patch of which the images are built 
 * at a specific level of detalisation.
 */
public final class Patch {

	public static final double INTENSITY_PRIOR_MEAN = 0.5;
	public static final double INTENSITY_PRIOR_MEAN_SAMPLES = 30.0;
	public static final double INTENSITY_PRIOR_VARIANCE = Math.pow(0.5, 2);
	public static final double INTENSITY_PRIOR_VARIANCE_SAMPLES = 30.0;
	public static final double LEG_DIR_ALPHA_TOTAL = 10.0;

	private final NIGDist _intensityDist;
	private DirDist _leg1PatchDist;
	private DirDist _leg2PatchDist;
	private DirDist _leg3PatchDist;
	private DirDist _leg4PatchDist;

	public Patch() {
		_intensityDist = new NIGDist(
				INTENSITY_PRIOR_MEAN, 
				INTENSITY_PRIOR_MEAN_SAMPLES,
				INTENSITY_PRIOR_VARIANCE,
				INTENSITY_PRIOR_VARIANCE_SAMPLES);
	}
	
	public double getIntensityLogProb(double intensity) {		
		// FIXME: calculate log prob within NIGDist!
		return Math.log(_intensityDist.getProb(intensity));
	}

	public double getLeg1LogProb(double[] patchProbs) {
		return _leg1PatchDist.getLogProb(patchProbs);
	}

	public double getLeg2LogProb(double[] patchProbs) {
		return _leg2PatchDist.getLogProb(patchProbs);
	}

	public double getLeg3LogProb(double[] patchProbs) {
		return _leg3PatchDist.getLogProb(patchProbs);
	}

	public double getLeg4LogProb(double[] patchProbs) {
		return _leg4PatchDist.getLogProb(patchProbs);
	}
	
	public void reset() {
		_intensityDist.reset();
		if (_leg1PatchDist != null) {
			_leg1PatchDist.reset();
			_leg2PatchDist.reset();
			_leg3PatchDist.reset();
			_leg4PatchDist.reset();
		}
	}
	
	public void onNextLayerCreated(final Layer nextLayer) {
		if (_leg1PatchDist != null) {
			throw new IllegalStateException("This patch already has a next layer");
		}
		final int nextDim = nextLayer.getDim();
		_leg1PatchDist = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
		_leg2PatchDist = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
		_leg3PatchDist = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
		_leg4PatchDist = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
	}

}
