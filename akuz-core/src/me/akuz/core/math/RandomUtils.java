package me.akuz.core.math;

import java.util.Random;

import Jama.Matrix;

public final class RandomUtils {
	
	public final static int nextInt(Random rnd, Matrix probs, int columnIndex) {
		
		double fraction = rnd.nextDouble();
		double sum = 0;
		
		for (int i=0; i<probs.getRowDimension(); i++) {
			sum += probs.get(i, columnIndex);
			if (sum >= fraction) {
				return i;
			}
		}
		
		return probs.getRowDimension()-1;
	}

}
