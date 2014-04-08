package me.akuz.core;

public final class Rounding {
	
	public static final double round(double value, int decimalPlaces) {
		double normalizer = Math.pow(10, decimalPlaces);
		return Math.round(value * normalizer) / normalizer;
	}

}
