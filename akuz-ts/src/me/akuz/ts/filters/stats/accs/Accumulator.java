package me.akuz.ts.filters.stats.accs;

public abstract class Accumulator<T extends Comparable<T>> {
	
	public abstract void reset();

	public abstract void add(final T time, final Object value);
	
	public abstract Object get();

}
