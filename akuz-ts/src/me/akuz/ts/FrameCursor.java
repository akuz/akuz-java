package me.akuz.ts;

import java.util.List;
import java.util.Map;

/**
 * {@link Frame} cursor provides information
 * about keys and items in a frame at a
 * particular (current) point in time.
 * 
 */
public interface FrameCursor<K, T extends Comparable<T>> {
	
	/**
	 * Get current time.
	 */
	T getCurrTime();
	
	/**
	 * Get keys within cursor.
	 */
	List<K> getKeys();
	
	/**
	 * Get current item by key.
	 */
	TItem<T> getCurrItem(K key);

	/**
	 * Get map of keys to current items.
	 */
	Map<K, TItem<T>> getCurrItems();
}
