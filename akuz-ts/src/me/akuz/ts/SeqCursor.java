package me.akuz.ts;

public interface SeqCursor<T extends Comparable<T>> {

	T getCurrTime();
	
	TItem<T> getCurrItem();
	
}
