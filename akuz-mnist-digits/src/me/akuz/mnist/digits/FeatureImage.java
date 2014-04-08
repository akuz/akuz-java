package me.akuz.mnist.digits;

/**
 * Feature image represents features detected in an underlying image.
 * Contains probability of features for each position (row x column).
 *
 */
public final class FeatureImage {
	
	private final double[][][] _data;
	
	public FeatureImage(int rowCount, int colCount, int dim) {
		if (rowCount <= 0) {
			throw new IllegalArgumentException("Row count must be positive");
		}
		if (colCount <= 0) {
			throw new IllegalArgumentException("Column count must be positive");
		}
		if (dim < 2) {
			throw new IllegalArgumentException("Dimensionality must be >= 2");
		}
		_data = new double[rowCount][colCount][dim];
	}
	
	public int getRowCount() {
		return _data.length;
	}
	
	public int getColCount() {
		return _data[0].length;
	}
	
	public int getDim() {
		return _data[0][0].length;
	}
	
	public double[] getFeatureProbs(int row, int col) {
		return _data[row][col];
	}

}
