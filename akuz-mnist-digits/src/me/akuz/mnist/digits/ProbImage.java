package me.akuz.mnist.digits;

import java.util.Arrays;

/**
 * Represents image with pixels containing discrete probability 
 * distributions over a fixed set of some underlying features.
 *
 */
public final class ProbImage {
	
	private final double[][][] _probs;
	
	public ProbImage(int rowCount, int colCount, int featureDim) {
		if (rowCount <= 0) {
			throw new IllegalArgumentException("Argument rowCount must be positive");
		}
		if (colCount <= 0) {
			throw new IllegalArgumentException("Argument colCount must be positive");
		}
		if (featureDim < 2) {
			throw new IllegalArgumentException("Argument featureDim must be >= 2");
		}
		_probs = new double[rowCount][colCount][featureDim];
		
		// fill with default max-ent values
		Arrays.fill(_probs, 1.0 / featureDim);
	}
	
	public int getRowCount() {
		return _probs.length;
	}
	
	public int getColCount() {
		return _probs[0].length;
	}
	
	public int getFeatureDim() {
		return _probs[0][0].length;
	}
	
	public double[] getFeatureProbs(final int row, final int col) {
		return _probs[row][col];
	}
}
