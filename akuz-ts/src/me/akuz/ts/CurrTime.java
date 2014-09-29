package me.akuz.ts;

/**
 * Helper functions for current time checks.
 *
 */
public final class CurrTime {
	
	/**
	 * Throws exception if the current time is not set.
	 * 
	 */
	public static <T extends Comparable<T>> void checkSet(final T currTime) {
		if (currTime == null) {
			throw new IllegalStateException("Current time is not set");
		}
	}
	
	/**
	 * Throws exception if the new current time is null, 
	 * or if it is not in strict chronological order 
	 * compared to the current time (if set).
	 * 
	 */
	public static <T extends Comparable<T>> void checkNew(final T currTime, final T newCurrTime) {
		if (newCurrTime == null) {
			throw new IllegalStateException("New current time is not set");
		}
		if (currTime != null && 
			currTime.compareTo(newCurrTime) >= 0) {
			throw new IllegalStateException(
					"Current time (" + currTime + 
					") must be < new current time (" + 
					newCurrTime + ")");
		}
	}
	
	/**
	 * Check current time and caller time are non-null and equal,
	 * otherwise throw exception.
	 * 
	 */
	public static <T extends Comparable<T>> void checkSame(final T currTime, final T callerTime) {
		if (currTime == null) {
			throw new IllegalStateException("Current time is not set");
		}
		if (callerTime == null) {
			throw new IllegalStateException("Caller time is not set");
		}
		if (!currTime.equals(callerTime)) {
			throw new IllegalStateException("Current times don't match");
		}
	}

}
