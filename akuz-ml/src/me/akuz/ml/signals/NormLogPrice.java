package me.akuz.ml.signals;

import Jama.Matrix;

public final class NormLogPrice {
	
	public static final Matrix calc(Matrix mLogPrice, Matrix mNormVol) {
		
		final Matrix m = new Matrix(mLogPrice.getRowDimension(), 1);
		m.set(0, 0, 0);
		for (int i=1; i<mLogPrice.getRowDimension(); i++) {
			final double ret = mLogPrice.get(i, 0) - mLogPrice.get(i-1, 0);
			final double normRet = ret / Math.sqrt(mNormVol.get(i, 0));
			m.set(i, 0, m.get(i-1, 0) + normRet);
		}
		return m;
	}

}
