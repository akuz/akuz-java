package me.akuz.ts;

import java.util.List;

import me.akuz.ts.sync.Synchronizable;

/**
 * 
 * {@link Seq} cursor provides information
 * about the item, if any, in a sequence at a
 * particular (current) point in time.
 * 
 */
public interface SeqCursor<T extends Comparable<T>>
extends Synchronizable<T> {
	
	/**
	 * Get current item, if any.
	 */
	TItem<T> getCurrItem();
	
	/**
	 * Get items moved during last time move,
	 * including the current item, if any.
	 */
	List<TItem<T>> getMovedItems();
	
}
