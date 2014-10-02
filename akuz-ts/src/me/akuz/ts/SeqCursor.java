package me.akuz.ts;

import java.util.List;

import me.akuz.ts.sync.TimeSchedule;

/**
 * Provides information about the current item, 
 * if any, in a sequence at a particular (current) 
 * point in time, and about the items "moved" 
 * during the last move through time.
 * 
 */
public interface SeqCursor<T extends Comparable<T>>
extends TimeSchedule<T> {
	
	/**
	 * Get underlying sequence.
	 */
	Seq<T> getSeq();
	
	/**
	 * Get next cursor index in
	 * the underlying sequence.
	 */
	int getNextCursor();
	
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
