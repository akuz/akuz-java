package me.akuz.ts.filters;

import java.util.List;

import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

/**
 * Base class for all 1D filters on single sequences
 * 
 * Filters determine the current rolling state based on 
 * the supplied blocks of items "moved" through time.
 * 
 * Examples: Last value, EWMA, Kalman Filter.
 *
 */
public abstract class Filter<T extends Comparable<T>> implements Cloneable {
	
	private String _fieldName;

	/**
	 * Notify filters about the next items moved through time;
	 * note that the currItem can be null, if there is no
	 * time series item at the current time; also note that 
	 * movedItems must contain all items since last call, 
	 * including the currItem (if it's not null), and 
	 * arranged in strict chronological order.
	 */
	public abstract void next(
			final TLog log,
			final T currTime, 
			final TItem<T> currItem, 
			final List<TItem<T>> movedItems);
	
	/**
	 * Get time series item that represents 
	 * the output state of this 1D filter
	 * at the current time.
	 */
	public abstract TItem<T> getCurrItem();
	
	/**
	 * Set filter field name (for logging).
	 */
	public final void setFieldName(final String fieldName) {
		_fieldName = fieldName;
	}
	
	/**
	 * Get filter field name (for logging).
	 */
	public final String getFieldName() {
		return _fieldName != null ? _fieldName : "unspecified";
	}
	
	/**
	 * Filter must be cloneable in order to be able
	 * to be used as a prototype for many sequences.
	 * Make sure to override the clone() method for
	 * nontrivial filters that contain pointers to
	 * data structures allocated on heap.
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
