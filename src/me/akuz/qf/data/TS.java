package me.akuz.qf.data;

import java.util.List;

/**
 * Time series abstract base class.
 *
 * @param <T> - Time type.
 */
public abstract class TS<T extends Comparable<T>> {
	
	public abstract List<TSEntry<T>> getSorted();

}
