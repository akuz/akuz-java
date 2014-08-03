package me.akuz.ml.signals;

import Jama.Matrix;

public final class ZeroBasedMaxExpVol {

	public static final Matrix calc(final Matrix mLogPrice, int[] halfLives) {

		if (mLogPrice.getColumnDimension() < 1) {
			throw new IllegalArgumentException("Input price matrix must have at least 1 column");
		}
		if (mLogPrice.getRowDimension() < 2) {
			throw new IllegalArgumentException("Input price matrix must have at least 2 rows");
		}
		if (halfLives.length < 1) {
			throw new IllegalArgumentException("Half lives array must have at least 1 entry");
		}
		
		// calculate lyamdas
		double[] decays = new double[halfLives.length];
		for (int i=0; i<decays.length; i++) {
			final int halfLife = halfLives[i];
			if (halfLife < 1) {
				throw new IllegalArgumentException("Half lives must be positive");
			}
			decays[i] = Math.exp(- Math.log(2) / halfLife);
		}
		
		// calculate matrix
		final Matrix m = new Matrix(mLogPrice.getRowDimension(), 1 + halfLives.length);
		
		// first row, no returns, no vol
		for (int j=0; j<halfLives.length+1; j++) {
			m.set(0, j, Double.NaN);
		}
		// second row, initialize vol
		{
			final int i = 1;
			final double ret = mLogPrice.get(i, 0) - mLogPrice.get(i-1, 0);
			final double setVol = ret*ret;
			double maxVol = 0;
			for (int j=0; j<decays.length; j++) {
				m.set(i, j+1, setVol);
				if (maxVol < setVol) {
					maxVol = setVol;
				}
			}
			m.set(i, 0, maxVol);
		}
		// third row and onwards, running vol
		for (int i=2; i<mLogPrice.getRowDimension(); i++) {
			
			final double ret = mLogPrice.get(i, 0) - mLogPrice.get(i-1, 0);
			final double addVol = ret*ret;
			
			double maxVol = 0;
			for (int j=0; j<decays.length; j++) {
				
				final double prevVol = m.get(i-1, j+1);
				final double currVol 
					= prevVol * decays[j]
					+ addVol * (1.0 - decays[j]);
				m.set(i, j+1, currVol);
				if (maxVol < currVol) {
					maxVol = currVol;
				}
			}
			m.set(i, 0, maxVol);
		}
		
		return m;
	}
}
