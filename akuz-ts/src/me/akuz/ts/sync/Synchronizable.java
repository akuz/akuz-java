package me.akuz.ts.sync;

import me.akuz.core.Out;

/**
 * Interface that must be implemented by 
 * anything that wants to be synchronized
 * by the {@link SynchronizeTimes}.
 * 
 * @author andrey
 *
 * @param <T>
 */
public interface Synchronizable<T extends Comparable<T>> {
	
	T getCurrTime();
	
	boolean getNextTime(final Out<T> nextTime);
	
	void moveToTime(final T time);

}
