package me.akuz.ts.align;

import me.akuz.ts.TItem;

public abstract class TSFiller<T extends Comparable<T>> implements Cloneable {

	public abstract TSAlignLogMsg next(T time, TItem<T> item);
	public abstract TItem<T> getCurrent();
	
	@Override
	@SuppressWarnings("unchecked")
	public TSFiller<T> clone() {
		try {
			return (TSFiller<T>)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Clone error");
		}
	}
}
