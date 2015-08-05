package me.akuz.mnist.digits.hrp;

import me.akuz.core.math.DirDist;
import me.akuz.core.math.NIGDist;

/**
 * Block of which the images are built.
 */
public final class ModelBlock {
	
	public static final double INTENSITY_PRIOR_MEAN = 0.0;
	public static final double INTENSITY_PRIOR_MEAN_SAMPLES = 30.0;
	public static final double INTENSITY_PRIOR_VARIANCE = 1.0;
	public static final double INTENSITY_PRIOR_VARIANCE_SAMPLES = 30.0;
	public static final double LEG_BLOCK_DIR_ALPHA = 10.0;
	
	private final int _level;
	private final int _indexInLevel;
	private final NIGDist _intensityDist;
	private DirDist _leg1BlockDist;
	private DirDist _leg2BlockDist;
	private DirDist _leg3BlockDist;
	private DirDist _leg4BlockDist;
	
	public ModelBlock(int level, int indexInLevel) {
		_level = level;
		_indexInLevel = indexInLevel;
		_intensityDist = new NIGDist(
				INTENSITY_PRIOR_MEAN, 
				INTENSITY_PRIOR_MEAN_SAMPLES,
				INTENSITY_PRIOR_VARIANCE,
				INTENSITY_PRIOR_VARIANCE_SAMPLES);
	}
	
	public void onNextLevelCreated(int dim) {
		_leg1BlockDist = new DirDist(dim, LEG_BLOCK_DIR_ALPHA / dim);
		_leg2BlockDist = new DirDist(dim, LEG_BLOCK_DIR_ALPHA / dim);
		_leg3BlockDist = new DirDist(dim, LEG_BLOCK_DIR_ALPHA / dim);
		_leg4BlockDist = new DirDist(dim, LEG_BLOCK_DIR_ALPHA / dim);
	}
	

}
