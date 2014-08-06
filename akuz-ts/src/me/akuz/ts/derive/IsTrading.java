package me.akuz.ts.derive;

import java.util.HashSet;
import java.util.Set;

import me.akuz.ts.TCube;
import me.akuz.ts.TFrame;

public final class IsTrading {
	
	/**
	 * Calculate trading mode for a individual stocks in 
	 * a portfolio, given the prices and isActive frames.
	 */
	public static final <K, T extends Comparable<T>>
	TFrame<K, T> calc(TFrame<K, T> prices, TFrame<K, T> isActive) {
		
		final TCube<Integer, K, T> cube = new TCube<>();
		cube.addFrame(0, prices);
		cube.addFrame(1, isActive);
		
		final Set<T> times = new HashSet<T>();
		cube.extractTimes(times);
		
		return null;
	}

}
