package me.akuz.mnist.digits;

import me.akuz.core.math.NIGDist;

/**
 * Pixel intensity feature of a fixed square size 
 * (size must be odd, so that we have a mid pixel).
 *
 */
public final class Feature {
	
	// need these constants to avoid zero probabilities
	private static final double MIN_PROB = 0.000000000000000000000000001;
	private static final double MIN_LOG_PROB = Math.log(MIN_PROB);
	
	// mid pixel index
	private final int _mid;
	private final NIGDist[][] _dist;
	
	/**
	 * Create feature of the given (odd) size, setting the same priors for each pixel intensity.
	 * 
	 * @param size
	 * @param priorMean
	 * @param priorMeanSamples
	 * @param priorVar
	 * @param priorVarSamples
	 */
	public Feature(
			int size, 
			double priorMean, 
			double priorMeanSamples, 
			double priorVar, 
			double priorVarSamples) {
		
		if (size < 3) {
			throw new IllegalArgumentException("Feature size must be >= 3");
		}
		if (size % 2 != 1) {
			throw new IllegalArgumentException("Feature size must be odd");
		}
		_mid = size / 2;
		_dist = new NIGDist[size][size];
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				_dist[i][j] = new NIGDist(priorMean, priorMeanSamples, priorVar, priorVarSamples);
			}
		}
	}
	
	/**
	 * Add pixel intensity observation, given shifts from the mid pixel.
	 * 
	 * @param iShift
	 * @param jShift
	 * @param value
	 * @param weight
	 */
	public void addObservation(int iShift, int jShift, double value, double weight) {

		if (Math.abs(iShift) > _mid) {
			return;
		}
		if (Math.abs(jShift) > _mid) {
			return;
		}
		_dist[_mid + iShift][_mid + jShift].addObservation(value, weight);
	}
	
	/**
	 * Get log prob of observing pixel intensity, given shifts from the mid pixel.
	 * 
	 * @param iShift
	 * @param jShift
	 * @param value
	 * @return
	 */
	public double getLogProb(int iShift, int jShift, double value) {
		if (Math.abs(iShift) > _mid) {
			return MIN_LOG_PROB;
		}
		if (Math.abs(jShift) > _mid) {
			return MIN_LOG_PROB;
		}
		return Math.log(_dist[_mid + iShift][_mid + jShift].getProb(value) + MIN_PROB);
	}

}
