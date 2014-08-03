package me.akuz.core.math;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Jama.Matrix;

public final class JamaUtils {
	
	public static double[] min(Matrix m, int[] dimensions) {
		
		double[] res = new double[dimensions.length];
		Arrays.fill(res, Double.NaN);
		
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int d=0; d<dimensions.length; d++) {
				int j = dimensions[d];
				double value = m.get(i, j);
				if (!Double.isNaN(value)) {
					if (Double.isNaN(res[d])) {
						res[d] = value;
					} else if (res[d] > value) {
						res[d] = value;
					}
				}
			}
		}
		
		return res;
	}
	
	public static double[] max(Matrix m, int[] dimensions) {
		
		double[] res = new double[dimensions.length];
		Arrays.fill(res, Double.NaN);
		
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int d=0; d<dimensions.length; d++) {
				int j = dimensions[d];
				double value = m.get(i, j);
				if (!Double.isNaN(value)) {
					if (Double.isNaN(res[d])) {
						res[d] = value;
					} else if (res[d] < value) {
						res[d] = value;
					}
				}
			}
		}
		
		return res;
	}
	
	public static double[] min(Matrix m, int minDimension, int dimensionCount) {
		
		double[] res = new double[dimensionCount];
		Arrays.fill(res, Double.NaN);
		
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<dimensionCount; j++) {
				int dimension = minDimension + j;
				double value = m.get(i, dimension);
				if (!Double.isNaN(value)) {
					if (Double.isNaN(res[j])) {
						res[j] = value;
					} else if (res[j] > value) {
						res[j] = value;
					}
				}
			}
		}
		
		return res;
	}
	
	public static double[] max(Matrix m, int minDimension, int dimensionCount) {
		
		double[] res = new double[dimensionCount];
		Arrays.fill(res, Double.NaN);
		
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<dimensionCount; j++) {
				int dimension = minDimension + j;
				double value = m.get(i, dimension);
				if (!Double.isNaN(value)) {
					if (Double.isNaN(res[j])) {
						res[j] = value;
					} else if (res[j] < value) {
						res[j] = value;
					}
				}
			}
		}
		
		return res;
	}
	
	public static double[] buckets(double min, double max, int bucketCount) {
		if (bucketCount < 1) {
			throw new InvalidParameterException("Buckets count should be positive");
		}
		
		double[] res = new double[bucketCount+1];
		
		res[0] = min;
		res[bucketCount] = max;
		double step = (max - min)/bucketCount;
		for (int i=1; i<bucketCount; i++) {
			res[i] = res[i-1] + step;
		}
		
		return res;
	}
	
	private static double updateMin(Matrix matrix, int dimension, int row, double currentMin) {
		if (Double.isNaN(currentMin)) {
			return matrix.get(row, dimension);
		} else {
			double value = matrix.get(row, dimension);
			return value < currentMin ? value : currentMin;
		}
	}
	
	private static double updateMax(Matrix matrix, int dimension, int row, double currentMax) {
		if (Double.isNaN(currentMax)) {
			return matrix.get(row, dimension);
		} else {
			double value = matrix.get(row, dimension);
			return value > currentMax ? value : currentMax;
		}
	}
	
	public static double[] buckets(int bucketCount, Matrix matrix, int dimension, int[] rows) {
		if (bucketCount < 1) {
			throw new InvalidParameterException("Buckets count should be positive");
		}

		double min = Double.NaN;
		double max = Double.NaN;
		
		if (rows == null) {
			for (int i=0; i<matrix.getRowDimension(); i++) {
				min = updateMin(matrix, dimension, i, min);
				max = updateMax(matrix, dimension, i, max);
			}
		} else {
			for (int k=0; k<rows.length; k++) {
				int i = rows[k];
				min = updateMin(matrix, dimension, i, min);
				max = updateMax(matrix, dimension, i, max);
			}
		}
		
		return buckets(min, max, bucketCount);
	}

	public static void printUniqueNumbers(Matrix m, int dimension) {
		List<Double> values = new ArrayList<Double>(m.getRowDimension());
		for (int i=0; i<m.getRowDimension(); i++) {
			values.add(m.get(i, dimension));
		}
		Collections.sort(values);
		
		int same = 1;
		int count = 0;
		Map<Integer, Integer> sameDistr = new TreeMap<Integer, Integer>();
		Double prevValue = null;
		for (int i=0; i<values.size(); i++) {
			Double value = values.get(i);
			if (prevValue == null || 
				Math.abs(value - prevValue) > 0.0001) {
				
				if (prevValue != null) {
					Integer sameCases = sameDistr.get(same);
					if (sameCases == null) {
						sameCases = 0;
					}
					sameCases++;
					sameDistr.put(same, sameCases);
				}
				
				count++;
				prevValue = value;
				same = 1;
			} else {
				same++;
			}
		}
		
		System.out.println("Unique values in dimension " + dimension + ": " + count);
		Iterator<Integer> same_i = sameDistr.keySet().iterator();
		while (same_i.hasNext()) {
			Integer sameCount = same_i.next();
			Integer sameCases = sameDistr.get(sameCount);
			System.out.println("Repeated " + sameCount + " values: " + sameCases + " cases");
		}
	}
	
	public static double[] bucketBorders(Matrix m, int dimension, int bucketCount) {
		if (bucketCount < 1) {
			throw new InvalidParameterException("Buckets count should be positive");
		}
		
		if (bucketCount >= m.getRowDimension()) {
			throw new InvalidParameterException("Buckets count must be less than number of rows in matrix");
		}
		
		int valuesPerBucket = m.getRowDimension()/bucketCount;
		List<Double> values = new ArrayList<Double>(m.getRowDimension());
		for (int i=0; i<m.getRowDimension(); i++) {
			values.add(m.get(i, dimension));
		}
		Collections.sort(values);
		
		double[] bucketBorders = new double[bucketCount+1];
		bucketBorders[0] = values.get(0);
		bucketBorders[bucketBorders.length-1] = values.get(values.size()-1);
		int bucketIndex = 0;
		int bucketValuesCount = 0;
		for (int i=0; i<values.size(); i++) {
			bucketValuesCount++;
			if (bucketValuesCount > valuesPerBucket) {
				bucketIndex++;
				if (bucketIndex >= bucketBorders.length - 1) {
					break;
				}
				bucketBorders[bucketIndex] = values.get(i);
				bucketValuesCount = 0;
			}
		}
		
		return bucketBorders;
	}

	public static int findBucketIndex(double[] bucketBorders, double value) {

		if (value < bucketBorders[0]) {
			throw new IllegalStateException("Value is smaller than buckets range");
		}
		if (value > bucketBorders[bucketBorders.length-1]) {
			throw new IllegalStateException("Value is larger than buckets range");
		}
		int idx = Arrays.binarySearch(bucketBorders, value);
		
		// if found
		int bucketIndex;
		if (idx >= 0) {
			// if equals last, correct the index
			if (idx == bucketBorders.length - 1) {
				idx = bucketBorders.length - 2;
			}
			bucketIndex = idx;
		} else {
			// based on binary search doc
			idx = -idx-1; 
			
			// if insert after last, correct
			if (idx == 0) {
				throw new IllegalStateException("Value is smaller than buckets range");
			}
			if (idx == bucketBorders.length) {
				throw new IllegalStateException("Value is larger than buckets range");
			}
			
			bucketIndex = idx-1;
		}

		return bucketIndex;
	}
	
	public static void normColsToOne(Matrix m) {
		double[] absSum = new double[m.getColumnDimension()];
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				absSum[j] += Math.abs(m.get(i, j));
			}
		}
		double equalWeight = 1.0/(double)m.getRowDimension();
		for (int j=0; j<m.getColumnDimension(); j++) {
			double jAbsSum = absSum[j];
			if (jAbsSum > 0) {
				for (int i=0; i<m.getRowDimension(); i++) {
					m.set(i, j, m.get(i, j) / jAbsSum);
				}
			} else {
				for (int i=0; i<m.getRowDimension(); i++) {
					m.set(i, j, equalWeight);
				}
			}
		}
	}
	
	public static double calcKLDiv(Matrix left, int colL, Matrix right, int colR) {
		
		if (left.getRowDimension() != right.getRowDimension()) {
			throw new InvalidParameterException("Matrices should have the same number of rows");
		}
		double divKL = 0.0;
		for (int i=0; i<left.getRowDimension(); i++) {
			double leftProb = left.get(i, colL);
			double rightProb = right.get(i, colR);
			if (leftProb > 0) {
				if (rightProb == 0) {
					throw new InvalidParameterException("KL-divergence: division by zero, incompatible distributions");
				}
				divKL += leftProb * (Math.log(leftProb + Double.MIN_NORMAL) - Math.log(rightProb + Double.MIN_NORMAL));
			}
		}
		return divKL;
	}
	
	public static double calcJSDist(Matrix left, int colL, Matrix right, int colR) {
		
		if (left.getRowDimension() != right.getRowDimension()) {
			throw new InvalidParameterException("Matrices should have the same number of rows");
		}
		double divKL1 = 0.0;
		double divKL2 = 0.0;
		for (int i=0; i<left.getRowDimension(); i++) {
			double leftProb = left.get(i, colL);
			double rightProb = right.get(i, colR);
			divKL1 += leftProb * (Math.log(leftProb + Double.MIN_NORMAL) - Math.log((rightProb + leftProb)/2.0 + Double.MIN_NORMAL));
			divKL2 += rightProb * (Math.log(rightProb + Double.MIN_NORMAL) - Math.log((rightProb + leftProb)/2.0 + Double.MIN_NORMAL));
		}
		return (divKL1 + divKL2)/2.0;
	}
	
	public static double calcProbCorr(Matrix left, int colL, Matrix right, int colR) {
		
		if (left.getRowDimension() != right.getRowDimension()) {
			throw new InvalidParameterException("Matrices should have the same number of rows");
		}
		double dist = 0.0;
		for (int i=0; i<left.getRowDimension(); i++) {
			double leftProb = left.get(i, colL);
			double rightProb = right.get(i, colR);
			
			dist += leftProb * rightProb;
		}
		return dist / left.getRowDimension();
	}
	
	/**
	 * Updates m to m.*x' without the need to create the transposed matrix x'.
	 * @param m
	 * @param x
	 */
	public static void updateMultTransposedElementWise(Matrix m, Matrix x) {
		if (m == null) {
			throw new NullPointerException("m");
		}
		if (x == null) {
			throw new NullPointerException("x");
		}
		if (m.getColumnDimension() != x.getRowDimension()) {
			throw new InvalidParameterException("Number of columns in m should be equal number of rows in x");
		}
		if (m.getRowDimension() != x.getColumnDimension()) {
			throw new InvalidParameterException("Number of rows in m should be equal number of columns in x");
		}
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				m.set(i, j, m.get(i, j)*x.get(j, i));
			}
		}
	}
	
	public static Matrix ones(int m, int n) {
		Matrix ones = new Matrix(m, n);
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				ones.set(i, j, 1);
			}
		}
		return ones;
	}
	
	public static Matrix randomAroundHalf(int m, int n, double fraction) {
		Matrix res = Matrix.random(m, n);
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				double value = res.get(i, j);
				value = 0.5 + (value - 0.5)* fraction;
				res.set(i, j, value);
			}
		}
		return res;
	}

	public static void normMatrixToOne(Matrix m) {
		double totalAbs = 0.0;
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				totalAbs += Math.abs(m.get(i, j));
			}
		}
		if (totalAbs > 0) {
			for (int i=0; i<m.getRowDimension(); i++) {
				for (int j=0; j<m.getColumnDimension(); j++) {
					m.set(i, j, m.get(i, j) / totalAbs);
				}
			}
		}
	}
	
	public static Matrix expectation2(Matrix m, Matrix probs) {
		if (m.getColumnDimension() != probs.getColumnDimension()) {
			throw new InvalidParameterException("Matrices should have the same column number");
		}
		if (probs.getRowDimension() !=1 ) {
			throw new InvalidParameterException("Probs matrix should have one row");
		}
		Matrix res = new Matrix(m.getRowDimension(), 1);
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				res.set(i, 0, res.get(i, 0) + m.get(i, j)*probs.get(0, j));
			}
		}
		return res;
	}
	
	public static void averageMatricesSaveToFirst(Matrix[] matrices) {
		for (int i=0; i<matrices[0].getRowDimension(); i++) {
			for (int j=0; j<matrices[0].getColumnDimension(); j++) {
				double average = matrices[0].get(i, j);
				for (int k=1; k<matrices.length; k++) {
					average = average / (k+1) * k + matrices[k].get(i, j) / (k+1);
				}
				matrices[0].set(i, j, average);
			}
		}
	}
	
	public static void averageMatricesWithWeightsSaveToFirst(Matrix matrix1, double weight1, Matrix matrix2, double weight2) {
		if (matrix1.getColumnDimension() != matrix2.getColumnDimension()) {
			throw new InvalidParameterException("Number of columns in matrices does not match");
		}
		if (matrix1.getRowDimension() != matrix2.getRowDimension()) {
			throw new InvalidParameterException("Number of wors in matrices does not match");
		}
		if (weight1 < 0) {
			throw new InvalidParameterException("Weights must be >= zero (weight1 is " + weight1 + ")");
		}
		if (weight2 < 0) {
			throw new InvalidParameterException("Weights must be >= zero (weight2 is " + weight2 + ")");
		}
		if (weight1 == 0 && weight2 == 0) {
			throw new InvalidParameterException("At least one of the weights must be > zero");
		}
		double totalWeight = weight1 + weight2;
		for (int i=0; i<matrix1.getRowDimension(); i++) {
			for (int j=0; j<matrix1.getColumnDimension(); j++) {
				double average = (weight1 / totalWeight) * matrix1.get(i, j) + (weight2 / totalWeight) * matrix2.get(i, j);
				matrix1.set(i, j, average);
			}
		}
	}

	public static final void timesReplace(Matrix m, double scalar) {
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				m.set(i, j, scalar * m.get(i, j));
			}
		}
	}
}
