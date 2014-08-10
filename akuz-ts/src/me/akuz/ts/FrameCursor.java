package me.akuz.ts;

import java.util.List;
import java.util.Map;

/**
 * Frame cursor interface provides information
 * about current time, keys, and items within
 * an iterator on a frame.
 * 
 */
public interface FrameCursor<K, T extends Comparable<T>> {
	
	T getCurrTime();
	
	List<K> getKeys();
	
	TItem<T> getCurrItem(K key);

	Map<K, TItem<T>> getCurrItems();
}
