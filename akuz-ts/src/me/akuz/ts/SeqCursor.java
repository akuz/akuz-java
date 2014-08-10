package me.akuz.ts;

/**
 * 
 * {@link Seq} cursor provides information
 * about the item, if any, in a sequence at a
 * particular (current) point in time.
 * 
 */
public interface SeqCursor<T extends Comparable<T>> {

	/**
	 * Get current time.
	 */
	T getCurrTime();
	
	/**
	 * Get current item, if any.
	 */
	TItem<T> getCurrItem();
	
}
