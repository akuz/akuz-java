package me.akuz.ml.signals;

import Jama.Matrix;

public final class ReturnsCalc {

	public static final Matrix geometric(Matrix values) {
		
		Matrix returns = new Matrix(values.getRowDimension(), values.getColumnDimension());
		
		// fill first row with NaNs
		for (int j=0; j<returns.getColumnDimension(); j++) {
			returns.set(0, j, Double.NaN);
		}
		
		// calculate the rest of the returns
		for (int i=1; i<values.getRowDimension(); i++) {
			for (int j=0; j<values.getColumnDimension(); j++) {
				
				double prevValue = values.get(i-1, j);
				double currValue = values.get(i, j);
				double ret = (currValue - prevValue) / prevValue;
				returns.set(i, j, ret);
			}
		}
		
		return returns;
	}
	
	public static final Matrix makeEMVARS(int columnIndex, Matrix returns, int startRowIndex, int[] lengths) {
		
		Matrix m = new Matrix(returns.getRowDimension(), lengths.length, Double.NaN);
		
		{ // initialize first values
			Double value0 = returns.get(startRowIndex, columnIndex);
			for (int j=0; j<m.getColumnDimension(); j++) {
				m.set(startRowIndex, j, Math.pow(value0, 2));
			}
		}
		
		// update the rest of the values
		for (int i=startRowIndex+1; i<returns.getRowDimension(); i++) {
			
			// set first column as original value
			Double ret = returns.get(i, columnIndex);
			
			// update the moving averages
			for (int j=0; j<lengths.length; j++) {
				
				double length = lengths[j];
				double alpha = 2.0 / (length + 1.0);
				
				double prevValue = m.get(i-1, j);
				double currValue = alpha * Math.pow(ret, 2) + (1.0 - alpha) * prevValue;
				m.set(i, j, currValue);
			}
		}
		
		return m;
	}

	public static final Matrix arithmetic(Matrix values) {
		
		Matrix returns = new Matrix(values.getRowDimension(), values.getColumnDimension());
		
		// fill first row with NaNs
		for (int j=0; j<returns.getColumnDimension(); j++) {
			returns.set(0, j, Double.NaN);
		}
		
		// calculate the rest of the returns
		for (int i=1; i<values.getRowDimension(); i++) {
			for (int j=0; j<values.getColumnDimension(); j++) {
				
				double prevValue = values.get(i-1, j);
				double currValue = values.get(i, j);
				double diff = currValue - prevValue;
				returns.set(i, j, diff);
			}
		}
		
		return returns;
	}
}
