package me.akuz.core.math;

public final class IntervalUtils {
	
	public static final double bound(double min, double max, double value) {
		
		if (min > max) {
			throw new IllegalArgumentException("Argument min should be <= max");
		}
		
		if (value < min) {
			return min;
		}
		
		if (value > max) {
			return max;
		}
		
		return value;
	}
	
	public static final int bound(int min, int max, int value) {
		
		if (min > max) {
			throw new IllegalArgumentException("Argument min should be <= max");
		}
		
		if (value < min) {
			return min;
		}
		
		if (value > max) {
			return max;
		}
		
		return value;
	}

}
