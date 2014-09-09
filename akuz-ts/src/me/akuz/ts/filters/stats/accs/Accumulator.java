package me.akuz.ts.filters.stats.accs;

public abstract class Accumulator<T extends Comparable<T>> implements Cloneable {
	
	public abstract void reset();

	public abstract void add(final T time, final Object value);
	
	public abstract Object get();
	
	@Override
	@SuppressWarnings("unchecked")
	public Accumulator<T> clone() {
		Accumulator<T> copy;
		try {
			copy = (Accumulator<T>)super.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Cloning error", e);
		}
	}

}
