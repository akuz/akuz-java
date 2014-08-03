package me.akuz.ml.signals;

import me.akuz.core.math.MatrixUtils;

import Jama.Matrix;


public final class OscillatorsData {
	
	private final Matrix _oscillatorsData;
	
	public OscillatorsData(Matrix values, int longLength, int mediumLength, int shortLength) {
		
		Matrix valuesLog = MatrixUtils.log(values);
		Matrix valuesLogAvgs = MovingMetrics.makeEMA(0, valuesLog, new int[] {mediumLength, longLength}, 0);
		Matrix valuesOsc1 = MatrixUtils.subtractColumns(valuesLogAvgs, 0, valuesLogAvgs, 1);
		Matrix valuesOsc1Avg = MovingMetrics.makeEMA(0, valuesOsc1, new int[] {shortLength}, 0);
		Matrix valuesOsc2 = MatrixUtils.subtractColumns(valuesOsc1, 0, valuesOsc1Avg, 0);
		_oscillatorsData = MatrixUtils.combineLeftAndRight(valuesOsc1, valuesOsc2);
	}
	
	public Matrix getDataMatrix() {
		return _oscillatorsData;
	}

}
