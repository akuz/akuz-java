package me.akuz.mnist.digits.hrp;

import me.akuz.core.geom.BWImage;
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

	public DirDist getChildrenFlagDist() {
		return null; // FIXME
	}

	public DirDist[][] getChildrenPatchDists() {
		return null; // _legPatchDists; // FIXME
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
		if (_legPatchDists != null) {
			for (int k=0; k<_legPatchDists.length; k++) {
				System.out.print("  Leg " + (k+1) +": ");
				System.out.println(_legPatchDists[k]);
			}
		}
		System.out.println();
		Image recon = new Image(-1, new BWImage(16, 16));
		reconstruct(1.0, recon, recon.getCenterX(), recon.getCenterY(), recon.getMinSize());
		System.out.println(recon.getBWImage().toAsciiArt());
	}
	
	public void reconstruct(
			final double weight,
			final Image image,
			final double centerX,
			final double centerY,
			final double size) {
		
		if (_legPatchDists == null) {
			
			image.addColor(
					centerX, 
					centerY, 
					size,
					weight * _intensityDist.getMeanMode());

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

			final Leg[] spreadLegs = spread.getLegs();
			for (int k=0; k<_legPatchDists.length; k++) {
				
				final Leg spreadLeg = spreadLegs[k];
				
				final double legCenterX = thisLeftX + spreadLeg.getCenterX() * size;
				final double legCenterY = thisLeftY + spreadLeg.getCenterY() * size;
				final double legSize = spreadLeg.getSize() * size;
				
				reconstructLeg(
						weight, 
						_legPatchDists[k], 
						image, 
						legCenterX, 
						legCenterY, 
						legSize);
			}
		}
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
		
//		SelectK<Integer, Double> select = new SelectK<>(SortOrder.Desc, 4);
//		for (int i=0; i<legPatchProbs.length; i++) {
//			select.add(new Pair<Integer, Double> (i, legPatchProbs[i]));
//		}
//		
//		final List<Pair<Integer,Double>> topPatchProbs = select.get();
//		// TODO: normalize
//		
//		for (final Pair<Integer, Double> pair : topPatchProbs) {
//			
//			nextPatches[pair.v1()].reconstruct(
//					weight * pair.v2(),
//					image,
//					centerX,
//					centerY,
//					size);
//			
//		}
		
		for (int i=0; i<nextPatches.length; i++) {
			
			nextPatches[i].reconstruct(
					weight * legPatchProbs[i],
					image,
					centerX,
					centerY,
					size);
		}
	}

}
