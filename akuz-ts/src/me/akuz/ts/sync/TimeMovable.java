package me.akuz.ts.sync;

/**
 * Interface that must be implemented by 
 * anything that wants to be synchronized
 * by the {@link SynchronizeTimes}.
 * 
 * @author andrey
 *
 * @param <T>
 */
public interface TimeMovable<T extends Comparable<T>> {
	
	/**
	 * Get current time.
	 */
	T getCurrTime();

	/**
	 * Move to the next time.
	 */
	void moveToTime(T time);

}
