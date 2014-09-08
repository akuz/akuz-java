package me.akuz.ts.filters.stats;

import me.akuz.ts.filters.stats.accs.CumsumAccumulator;

/**
 * Cumsum filter (1D).
 *
 */
public class CumsumFilter<T extends Comparable<T>> extends AccumulatorFilter<T> {
	
	public CumsumFilter() {
		super(new CumsumAccumulator<T>());
	}
	
	public CumsumFilter(final double startValue) {
		super(new CumsumAccumulator<T>(startValue));
	}

}
