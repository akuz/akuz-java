package me.akuz.ml.signals;

import java.security.InvalidParameterException;

import me.akuz.core.math.SampleAverage;


import Jama.Matrix;

public final class MovingMetrics {

	public final static Matrix makeEMA(int columnIndex, Matrix sourceMatrix, int[] lengths, int startRow) {
		
		Matrix m = new Matrix(sourceMatrix.getRowDimension(), lengths.length);
		
		{ // initialize first values
			Double value0 = sourceMatrix.get(startRow, columnIndex);
			for (int j=0; j<m.getColumnDimension(); j++) {
				m.set(startRow, j, value0);
			}
		}
		
		// update the rest of the values
		for (int i=startRow+1; i<sourceMatrix.getRowDimension(); i++) {
			
			// set first column as original value
			Double value = sourceMatrix.get(i, columnIndex);
			
			// update the moving averages
			for (int j=0; j<lengths.length; j++) {
				
				double length = lengths[j];
				double alpha = 2.0 / (length + 1.0);
				
				double prevValue = m.get(i-1, j);
				double currValue = alpha * value + (1.0 - alpha) * prevValue;
				m.set(i, j, currValue);
			}
		}
		
		return m;
	}

	public final static Matrix makeEMA(Matrix m, int[] lengths, int startRow) {
		
		if (m.getColumnDimension() != lengths.length) {
			throw new InvalidParameterException("Input matrix columns number and the number of entries in lengths array should be the same");
		}

		Matrix result = new Matrix(m.getRowDimension(), m.getColumnDimension());
		
		{ // initialize first values
			for (int j=0; j<m.getColumnDimension(); j++) {
				Double value0 = m.get(startRow, j);
				result.set(startRow, j, value0);
			}
		}
		
		// update the rest of the values
		for (int i=startRow+1; i<m.getRowDimension(); i++) {
			
			// update the moving averages
			for (int j=0; j<m.getColumnDimension(); j++) {
				
				double length = lengths[j];
				double alpha = 2.0 / (length + 1.0);
				
				Double value = m.get(i, j);

				double prevValue = result.get(i-1, j);
				double currValue = alpha * value + (1.0 - alpha) * prevValue;
				result.set(i, j, currValue);
			}
		}
		
		return result;
	}

	public final static Matrix makeMA(Matrix m, int length) {
		
		Matrix result = new Matrix(m.getRowDimension(), m.getColumnDimension(), Double.NaN);
		
		// update the rest of the values
		for (int i=length-1; i<m.getRowDimension(); i++) {
			
			// initialize the moving averages
			for (int j=0; j<m.getColumnDimension(); j++) {
				result.set(i, j, 0.0);
			}
			
			// update the moving averages
			for (int k=i-length+1; k<=i; k++) {
				for (int j=0; j<m.getColumnDimension(); j++) {
					Double value = m.get(k, j);
					result.set(i, j, result.get(i, j) + value);
				}
			}
			
			// finalize the moving averages
			for (int j=0; j<m.getColumnDimension(); j++) {
				result.set(i, j, result.get(i, j) / (double)length);
			}
		}
		
		return result;
	}

	public final static Matrix smoothAvg(final Matrix m, final int iMin, final int iMax, final int offsetLeft, final int offsetRight) {
		
		Matrix result = new Matrix(m.getRowDimension(), m.getColumnDimension(), Double.NaN);
		
		// update the rest of the values
		for (int i=0; i<m.getRowDimension(); i++) {

			// calc span
			int spanMin = i + offsetLeft;
			int spanMax = i + offsetRight;
			
			// check span
			if (spanMin < iMin || spanMax > iMax) {
				continue;
			}
			
			// calc average
			for (int j=0; j<m.getColumnDimension(); j++) {
				SampleAverage avg = new SampleAverage();
				for (int k=spanMin; k<=spanMax; k++) {
					avg.add(m.get(k, j));
				}
				result.set(i, j, avg.getMean());
			}
		}
		
		return result;
	}

	public final static Matrix cumsum(Matrix m, int startRow) {
		
		Matrix result = new Matrix(m.getRowDimension(), m.getColumnDimension(), Double.NaN);
		
		// update the rest of the values
		for (int i=startRow; i<m.getRowDimension(); i++) {
			
			if (i==startRow) {

				// initialize the cumulative sums
				for (int j=0; j<m.getColumnDimension(); j++) {
					result.set(i, j, m.get(i, j));
				}
				
			} else {
				
				// update the cumulative sums
				for (int j=0; j<m.getColumnDimension(); j++) {
					result.set(i, j, result.get(i-1, j) + m.get(i, j));
				}
			}
		}
		
		return result;
	}
	
	public final static Matrix makeSlopes(Matrix m, int[] halfLifes) {
		
		if (m.getColumnDimension() > halfLifes.length) {
			throw new InvalidParameterException("Half lifes should be provided for all columns of the matrix (got " + halfLifes.length + " < " + m.getColumnDimension() + ")");
		}
		Matrix result = new Matrix(m.getRowDimension(), m.getColumnDimension(), Double.NaN);

		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				
				// get half life
				int halfLife = halfLifes[j];
				double alpha = 2.0 / (halfLife + 1.0);
				int samplesNeeded = 2 * halfLife;

				if (i>=samplesNeeded-1) {

					// calculate exponential weights matrix
					Matrix W = new Matrix(samplesNeeded, samplesNeeded, 0);
					double currentMass = 1.0;
					for (int k=samplesNeeded-1; k>=0; k--) {
						
						double weight = currentMass * alpha;
						W.set(k, k, weight);
						currentMass -= weight;
					}
					
					// collect the A and b matrices
					Matrix A = new Matrix(samplesNeeded, 2);
					Matrix b = new Matrix(samplesNeeded, 1);
					for (int k=0; k<samplesNeeded; k++) {
						
						double value = m.get(i - samplesNeeded + 1 + k, j);
						A.set(k, 0, k);
						A.set(k, 1, 1.0);
						b.set(k, 0, value);
					}
					
					Matrix WA = W.times(A);
					Matrix WA_t = WA.transpose();
					Matrix Wb = W.times(b);
					
					Matrix WA_t_WA = WA_t.times(WA);
					Matrix WA_t_WA_inv = WA_t_WA.inverse();
					
					Matrix x = WA_t_WA_inv.times(WA_t).times(Wb);
					double slope = x.get(0,0);
					
					result.set(i, j, slope);
				}
			}
		}
		
		return result;
	}
}
