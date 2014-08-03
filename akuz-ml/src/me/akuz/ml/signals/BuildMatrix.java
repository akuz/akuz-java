package me.akuz.ml.signals;

import java.util.Date;
import java.util.List;

import me.akuz.core.Pair;


import Jama.Matrix;

public final class BuildMatrix {
	
	public static final Matrix logFromSinglePriceData(List<Pair<Double, Date>> prices) {
		
		Matrix m = new Matrix(prices.size(), 1);
		
		for (int i=0; i<prices.size(); i++) {
			m.set(i, 0, Math.log(prices.get(i).v1()));
		}
		
		return m;
	}

}
