package me.akuz.ts.align;

import me.akuz.ts.TSItem;

public abstract class TSFiller<T extends Comparable<T>> implements Cloneable {

	public abstract TSAlignLogMsg next(T time, TSItem<T> item);
	public abstract TSItem<T> getCurrent();
	
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
