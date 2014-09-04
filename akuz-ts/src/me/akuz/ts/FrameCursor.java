package me.akuz.ts;

import java.util.List;
import java.util.Map;

import me.akuz.ts.sync.Synchronizable;

/**
 * {@link Frame} cursor provides information
 * about keys and items in a frame at a
 * particular (current) point in time.
 * 
 */
public interface FrameCursor<K, T extends Comparable<T>>
extends Synchronizable<T> {
	
	/**
	 * Get keys within cursor.
	 */
	List<K> getKeys();
	
	/**
	 * Get current item by key.
	 */
	TItem<T> getCurrItem(final K key);
	
	/**
	 * Get map of keys to current items.
	 */
	Map<K, TItem<T>> getCurrItems();
	
	/**
	 * Get list of items moved during last time 
	 * move, including the current items, if any.
	 */
	List<TItem<T>> getMovedItems(final K key);

	/**
	 * Get map by key of items moved during last time 
	 * move, including the current items, if any.
	 */
	Map<K, List<TItem<T>>> getMovedItems();

}
