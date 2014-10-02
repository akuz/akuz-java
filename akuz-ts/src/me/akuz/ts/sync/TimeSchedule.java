package me.akuz.ts.sync;

import me.akuz.core.Out;

/**
 * In addition to being TimeMovable itself, it also provides 
 * a way to get the next time, to which this TimeMovable object 
 * can be moved to (based on the next available data it contains).
 * 
 */
public interface TimeSchedule<T extends Comparable<T>> extends TimeMovable<T> {
	
	/**
	 * Get next available time, if any.
	 * Returns true, if the next time was 
	 * populated in the output argument.
	 */
	boolean getNextTime(Out<T> nextTime);

}
