package me.akuz.core;

public final class Rounding {
	
	public static final double round(final double value, final int decimalPlaces) {
		final double normalizer = Math.pow(10, decimalPlaces);
		return Math.round(value * normalizer) / normalizer;
	}

}
