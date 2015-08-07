package me.akuz.mnist.digits.hrp;

import me.akuz.core.math.DirDist;
import me.akuz.core.math.NIGDist;

/**
 * Patch of which the images are built.
 */
public final class Patch {
	
	public static final double COLOR_PRIOR_MEAN = 0.0;
	public static final double COLOR_PRIOR_MEAN_SAMPLES = 30.0;
	public static final double COLOR_PRIOR_VARIANCE = 1.0;
	public static final double COLOR_PRIOR_VARIANCE_SAMPLES = 30.0;
	public static final double LEG_DIR_ALPHA_TOTAL = 10.0;

	private final NIGDist _colorDist;
	private DirDist _leg1PatchDist;
	private DirDist _leg2PatchDist;
	private DirDist _leg3PatchDist;
	private DirDist _leg4PatchDist;

	public Patch() {
		_colorDist = new NIGDist(
				COLOR_PRIOR_MEAN, 
				COLOR_PRIOR_MEAN_SAMPLES,
				COLOR_PRIOR_VARIANCE,
				COLOR_PRIOR_VARIANCE_SAMPLES);
	}
	
	public void onNextLayerCreated(final Layer nextLayer) {
		final int nextDim = nextLayer.getDim();
		_leg1PatchDist = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
		_leg2PatchDist = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
		_leg3PatchDist = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
		_leg4PatchDist = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
	}

}
