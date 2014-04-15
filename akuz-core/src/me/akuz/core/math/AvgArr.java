package me.akuz.core.math;

import java.util.Arrays;

/**
 * Calculates element-wise average of arrays.
 *
 */
public final class AvgArr {
	
	private final double[] _avg;
	private int _count;
	
	public AvgArr(final int size) {
		this(size, 0.0);
	}
	
	public AvgArr(final int size, final double fill) {
		_avg = new double[size];
		Arrays.fill(_avg, fill);
	}
	
	public void add(double[] val) {
		if (_avg.length != val.length) {
			throw new IllegalArgumentException("Dimensionality mismatch");
		}
		if (_count == 0) {
			for (int i=0; i<_avg.length; i++) {
				_avg[i] = val[i];
			}
		} else {
			for (int i=0; i<_avg.length; i++) {
				_avg[i] = _avg[i] / (double) (_count+1) * (double) _count 
						+  val[i] / (double) (_count+1);
			}
		}
		_count++;
	}
	
	public double[] get() {
		return _avg;
	}

}
