package me.akuz.mnist.digits.hrp;

import me.akuz.core.geom.ByteImage;
import me.akuz.core.math.DirDist;
import me.akuz.core.math.NIGDist;

/**
 * Patch of which the images are built 
 * at a specific layer of detalisation.
 */
public final class Patch {

	// these numbers are ok, not very important now
	public static final double INTENSITY_PRIOR_MEAN = 0.5;
	public static final double INTENSITY_PRIOR_MEAN_RANGE = 0.1;
	public static final double INTENSITY_PRIOR_MEAN_SAMPLES = 10.0;
	public static final double INTENSITY_PRIOR_VARIANCE = Math.pow(0.2, 2);
	public static final double INTENSITY_PRIOR_VARIANCE_SAMPLES = 10.0;
	
	// we want each leg distribution to be concentrated
	// on a few new layer patches, so we need a low alpha
	public static final double LEG_DIR_ALPHA_TOTAL = 1.0;
	
	// layer of this patch
	private final Layer _layer;

	// average intensity distribution of this patch
	private final NIGDist _intensityDist;
	
	// this patch legs distributions over next layer patches
	private DirDist[] _legPatchDists;
	private Layer _nextLayer;

	/**
	 * Construct a new patch.
	 * 
	 * @param initNoise - to differentiate patches, must be within [0, 1].
	 */
	public Patch(final Layer layer, final double initNoise) {
		if (initNoise < 0.0 || initNoise > 1.0) {
			throw new IllegalArgumentException(
					"Initial noise must be within [0, 1], but got " + initNoise);
		}
		_layer = layer;
		_intensityDist = new NIGDist(
				INTENSITY_PRIOR_MEAN + (initNoise - 0.5) * INTENSITY_PRIOR_MEAN_RANGE, 
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
		final Spread spread = _layer.getSpread();
		_legPatchDists = new DirDist[spread.getLegCount()];
		for (int k=0; k<_legPatchDists.length; k++) {
			_legPatchDists[k] = new DirDist(nextDim, LEG_DIR_ALPHA_TOTAL / nextDim);
			_legPatchDists[k].normalize();
		}
		_nextLayer = nextLayer;
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
		System.out.println();
		Image recon = new Image(-1, new ByteImage(10, 10));
		reconstruct(1.0, recon, recon.getCenterX(), recon.getCenterY(), recon.getMinSize());
		System.out.println(recon.getByteImage().toAsciiArt());
	}
	
	private void reconstructLeg(
			final double weight,
			final DirDist legPatchDist,
			final Image image,
			final double centerX,
			final double centerY,
			final double size) {
		
		if (_nextLayer == null) {
			throw new IllegalStateException(
					"Expected next layer when reconstructing patch leg");
		}
		
		Patch[] nextPatches = _nextLayer.getPatches();
		if (nextPatches.length != legPatchDist.getDim()) {
			throw new IllegalStateException(
					"Next layer patch cound doesn't match legPatchDist dim");
		}

		double[] legPatchProbs = legPatchDist.getPosteriorMean();
		
		for (int i=0; i<nextPatches.length; i++) {
			
			nextPatches[i].reconstruct(
					weight * legPatchProbs[i],
					image,
					centerX,
					centerY,
					size);
		}
	}
	
	public void reconstruct(
			final double weight,
			final Image image,
			final double centerX,
			final double centerY,
			final double size) {
		
		if (_legPatchDists == null) {
			
			image.addIntensity(centerX, centerY, size, _intensityDist.getMeanMode() * weight);
			
		} else {
			
			final Spread spread = _layer.getSpread();
			
			if (_legPatchDists.length != spread.getLegCount()) {
				throw new IllegalStateException(
						"Unexpected number of patch legs: " + 
						_legPatchDists.length + ", while " +
						"layer spread has " + spread.getLegCount());
			}
			
			final double halfSize = size / 2.0;
			final double thisLeftX = centerX - halfSize;
			final double thisLeftY = centerY - halfSize;

			final SpreadLeg[] spreadLegs = spread.getLegs();
			for (int k=0; k<_legPatchDists.length; k++) {
				
				final SpreadLeg spreadLeg = spreadLegs[k];
				
				final double legCenterX = thisLeftX + spreadLeg.getCenterX() * size;
				final double legCenterY = thisLeftY + spreadLeg.getCenterY() * size;
				final double legSize = spreadLeg.getSize() * size;
				
				reconstructLeg(weight, _legPatchDists[k], image, legCenterX, legCenterY, legSize);
			}
		}
	}

}
