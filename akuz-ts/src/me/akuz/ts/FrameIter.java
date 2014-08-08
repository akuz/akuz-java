package me.akuz.ts;

import java.util.Map;

/**
 * Frame iterator interface.
 * 
 */
public interface FrameIter<K, T extends Comparable<T>> {

	public boolean hasNext();
	
	public void next();

	public T getCurrTime();
	
	public Map<K, TItem<T>> getCurrItems();

}
