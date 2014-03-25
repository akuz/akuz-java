package me.akuz.ts.align;

import me.akuz.ts.TSItem;

public abstract class TSChecker<T extends Comparable<T>> implements Cloneable {

	public abstract TSAlignLogMsg next(T time, TSItem<T> item);
	
	@Override
	@SuppressWarnings("unchecked")
	protected TSChecker<T> clone() {
		try {
			return (TSChecker<T>)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Clone error");
		}
	}
}
