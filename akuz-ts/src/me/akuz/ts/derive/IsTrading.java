package me.akuz.ts.derive;

import me.akuz.ts.Cube;
import me.akuz.ts.Frame;

public final class IsTrading {
	
	/**
	 * Calculate trading mode for a individual stocks in 
	 * a portfolio, given the prices and isActive frames.
	 */
	public static final <K, T extends Comparable<T>>
	Frame<K, T> calc(Frame<K, T> prices, Frame<K, T> isActive) {
		
		final Cube<Integer, K, T> cube = new Cube<>();
		cube.addFrame(0, prices);
		cube.addFrame(1, isActive);
		
		return null;
	}

}
