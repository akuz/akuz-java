package me.akuz.ts.filters;

import java.util.List;

import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

/**
 * Base class for rolling states of sequences, which 
 * determine the current rolling value based on the 
 * previous sequence of items in a sequence.
 * 
 * Examples: Last value, EWMA, Kalman Filter.
 *
 */
public abstract class Filter<T extends Comparable<T>> implements Cloneable {

	/**
	 * Notify state about the next items at a given time;
	 * note that the currItem can be null, if the sequence
	 * doesn't have an item at that time; also note that 
	 * movedItems contains all items since last call, 
	 * including the currItem, if it's not null.
	 */
	public abstract void next(
			final TLog log,
			final T currTime, 
			final TItem<T> currItem, 
			final List<TItem<T>> movedItems);
	
	/**
	 * Get item that represents the current state.
	 */
	public abstract TItem<T> getCurrent();
	
	/**
	 * Set name of the field for logging.
	 */
	public abstract void setFieldName(
			final String fieldName);
	
	/**
	 * State must be cloneable in order to be able
	 * to be used as a prototype for many sequences.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Filter<T> clone() {
		try {
			return (Filter<T>)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Clone error");
		}
	}
}
