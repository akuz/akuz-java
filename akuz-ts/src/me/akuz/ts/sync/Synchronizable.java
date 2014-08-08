package me.akuz.ts.sync;

/**
 * Interface that must be implemented by 
 * anything that wants to be synchronized
 * by the {@link Synchronizer}.
 * 
 * @author andrey
 *
 * @param <T>
 */
public interface Synchronizable<T extends Comparable<T>> {
	
	void moveToTime(final T time);

}
