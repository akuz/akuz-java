package me.akuz.ts;

import java.util.List;

import me.akuz.ts.log.TLog;

/**
 * Base class for all 1D filters on single sequences
 * 
 * Filters determine the current rolling state based on 
 * the supplied blocks of items "moved" through time.
 * 
 * Examples: Last value, EWMA, 1D Kalman Filter.
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
			final TLog<T> log,
			final T time, 
			final SeqCursor<T> iter);
	
	/**
	 * Get TItem<T> that represents the
	 * output state, if any, of this 
	 * filter at the *current* time.
	 */
	public abstract TItem<T> getCurrItem();
	
	/**
	 * Get a list of TItem<T>s containing
	 * all intermediate states, if any, 
	 * which this filter went through 
	 * during the last call of next();
	 * this list also includes the
	 * *current* TItem<T>, if any.
	 */
	public abstract List<TItem<T>> getMovedItems();
	
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
