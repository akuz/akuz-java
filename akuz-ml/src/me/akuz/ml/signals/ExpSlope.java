package me.akuz.ml.signals;

import me.akuz.core.math.StatsUtils;
import me.akuz.core.math.WeightedAverage;
import Jama.Matrix;

public final class ExpSlope {

	public static final void calculateVerticalBackward(
			int twoHalfLives, 
			Matrix inputM, 
			int inputRow, 
			int inputColumn,
			Matrix outputM,
			int outputRow,
			Integer outputSlopeColumn,
			Integer outputInterceptColumn,
			Integer outputVarianceColumn) {
		
		final double halfLife = (double) twoHalfLives / 2.0;
		
		final int firstIndex = inputRow - twoHalfLives + 1;
		final int lastIndex  = inputRow;
		
		Matrix W = new Matrix(twoHalfLives, twoHalfLives);
		Matrix A = new Matrix(twoHalfLives, 2);
		Matrix b = new Matrix(twoHalfLives, 1);
		
		for (int i=firstIndex; i<=lastIndex; i++) {

			final int idx = i-firstIndex;
			final int shift = i-lastIndex;
			
			double weight = StatsUtils.calcDistanceWeightExponential(shift, halfLife);
			W.set(idx, idx, weight);
			
			A.set(idx, 0, shift);
			A.set(idx, 1, 1.0);
			
			double value = inputM.get(i, inputColumn);
			b.set(idx, 0, value);
		}
		
		Matrix WA = W.times(A);
		Matrix WA_t = WA.transpose();
		Matrix Wb = W.times(b);
		
		Matrix WA_t_WA = WA_t.times(WA);
		Matrix WA_t_WA_inv = WA_t_WA.inverse();
		
		Matrix x = WA_t_WA_inv.times(WA_t).times(Wb);
		final double slope = x.get(0, 0);
		final double intercept = x.get(1, 0);
		
		if (outputSlopeColumn != null) {
			outputM.set(outputRow, outputSlopeColumn, slope);
		}
		if (outputInterceptColumn != null) {
			outputM.set(outputRow, outputInterceptColumn, intercept);
		}
		if (outputVarianceColumn != null) {
			WeightedAverage variance = new WeightedAverage();
			for (int i=firstIndex; i<=lastIndex; i++) {
				final int shift = i-lastIndex;
				double weight = StatsUtils.calcDistanceWeightExponential(shift, halfLife);
				double value = inputM.get(i, inputColumn);
				double approxValue = intercept + shift * slope;
				variance.add(Math.pow(value - approxValue, 2), weight);
			}
			outputM.set(outputRow, outputVarianceColumn, variance.get());
		}
	}

	public static final void calculateVerticalForward(
			int twoHalfLives, 
			Matrix inputM, 
			int inputRow, 
			int inputColumn,
			Matrix outputM,
			int outputRow,
			Integer outputSlopeColumn,
			Integer outputInterceptColumn) {
		
		final double halfLife = (double) twoHalfLives / 2.0;
		
		final int firstIndex = inputRow;
		final int lastIndex  = inputRow + twoHalfLives - 1;
		
		Matrix W = new Matrix(twoHalfLives, twoHalfLives);
		Matrix A = new Matrix(twoHalfLives, 2);
		Matrix b = new Matrix(twoHalfLives, 1);
		
		for (int i=firstIndex; i<=lastIndex; i++) {

			final int idx = i-firstIndex;
			final int shift = idx;
			
			double weight = StatsUtils.calcDistanceWeightExponential(shift, halfLife);
			W.set(idx, idx, weight);
			
			A.set(idx, 0, shift);
			A.set(idx, 1, 1.0);
			
			double value = inputM.get(i, inputColumn);
			b.set(idx, 0, value);
		}
		
		Matrix WA = W.times(A);
		Matrix WA_t = WA.transpose();
		Matrix Wb = W.times(b);
		
		Matrix WA_t_WA = WA_t.times(WA);
		Matrix WA_t_WA_inv = WA_t_WA.inverse();
		
		Matrix x = WA_t_WA_inv.times(WA_t).times(Wb);
		final double slope = x.get(0, 0);
		final double intercept = x.get(1, 0);
		
		if (outputSlopeColumn != null) {
			outputM.set(outputRow, outputSlopeColumn, slope);
		}
		if (outputInterceptColumn != null) {
			outputM.set(outputRow, outputInterceptColumn, intercept);
		}
	}

}
