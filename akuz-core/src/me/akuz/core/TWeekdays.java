package me.akuz.core;

/**
 * Weekdays calculations.
 *
 */
public final class TWeekdays {
	
	/**
	 * Throws exception if the date is *not* a weekday.
	 * 
	 */
	public static void checkWeekday(final TDate date) {
		final int dow = date.getDayOfWeek();
		if (dow == 6) {
			throw new IllegalArgumentException(
					"Saturday " + date + " is not a weekday");
		}
		if (dow == 7) {
			throw new IllegalArgumentException(
					"Sunday " + date + " is not a weekday");
		}
	}
	
	/**
	 * Returns true, if the date is a weekday.
	 * 
	 */
	public static boolean isWeekday(final TDate date) {
		final int dow = date.getDayOfWeek();
		return dow != 6 && dow != 7;
	}

	/**
	 * Returns first weekday on or after the given date.
	 * 
	 */
	public static TDate first(final TDate date) {
		final int dow = date.getDayOfWeek();
		if (dow == 6) {
			// Saturday >> Monday
			return date.plusDays(2);
		}
		if (dow == 7) {
			// Sunday >> Monday
			return date.plusDays(1);
		}
		return date;
	}

	/**
	 * Returns last weekday on or before the given date.
	 * 
	 */
	public static TDate last(TDate date) {
		final int dow = date.getDayOfWeek();
		if (dow == 6) {
			// Friday << Saturday
			return date.plusDays(-1);
		}
		if (dow == 7) {
			// Friday << Sunday
			return date.plusDays(-2);
		}
		return date;
	}
	
	/**
	 * Returns next weekday (only accepts weekday arguments).
	 * 
	 */
	public static TDate next(final TDate date) {
		checkWeekday(date);
		return first(date.plusDays(1));
	}
	
	/**
	 * Returns previous weekday (only accepts weekday arguments).
	 * 
	 */
	public static TDate prev(final TDate date) {
		checkWeekday(date);
		return last(date.plusDays(-1));
	}
	
	/**
	 * Add a number of weekdays (only accepts weekday arguments).
	 * 
	 */
	public static TDate add(final TDate date, final int weekdays) {
		checkWeekday(date);
		if (weekdays >= 5) {
			final int fullWeeks = (int)Math.floor(weekdays/5.0);
			final int remaining = weekdays - fullWeeks * 5;
			final TDate afterWeeks = date.plusWeeks(fullWeeks);
			return remaining == 0 ? afterWeeks : add(afterWeeks, remaining);
		} else if (weekdays <= -5) {
			final int fullWeeks = (int)Math.ceil(weekdays/5.0);
			final int remaining = weekdays - fullWeeks * 5;
			final TDate afterWeeks = date.plusWeeks(fullWeeks);
			return remaining == 0 ? afterWeeks : add(afterWeeks, remaining);
		} else if (weekdays > 0) {
			TDate result = date;
			for (int i=0; i<weekdays; i++) {
				result = next(result);
			}
			return result;
		} else if (weekdays < 0) {
			TDate result = date;
			for (int i=0; i>weekdays; i--) {
				result = prev(result);
			}
			return result;
		} else {
			return date;
		}
	}
}
