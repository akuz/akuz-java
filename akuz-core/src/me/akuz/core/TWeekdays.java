package me.akuz.core;

/**
 * Weekdays calculations.
 *
 */
public final class TWeekdays {

	/**
	 * Returns first weekday on or after the given date.
	 * 
	 */
	public TDate first(final TDate date) {
		final int dow = date.getDayOfWeek();
		if (dow == 6) {
			// Saturday >> Monday
			return date.plusDays(2);
		}
		if (dow == 0) {
			// Sunday >> Monday
			return date.plusDays(1);
		}
		return date;
	}

	/**
	 * Returns last weekday on or before the given date.
	 * 
	 */
	public TDate last(TDate date) {
		final int dow = date.getDayOfWeek();
		if (dow == 6) {
			// Friday << Saturday
			return date.plusDays(-1);
		}
		if (dow == 0) {
			// Friday << Sunday
			return date.plusDays(-2);
		}
		return date;
	}
	
	/**
	 * Returns next weekday (only accepts weekday arguments).
	 * 
	 */
	public TDate next(final TDate date) {
		final int dow = date.getDayOfWeek();
		if (dow == 6) {
			throw new IllegalArgumentException(
					"Saturday " + date + " is not a weekday");
		}
		if (dow == 0) {
			throw new IllegalArgumentException(
					"Sunday " + date + " is not a weekday");
		}
		return first(date.plusDays(1));
	}
	
	/**
	 * Returns previous weekday (only accepts weekday arguments).
	 * 
	 */
	public TDate prev(final TDate date) {
		final int dow = date.getDayOfWeek();
		if (dow == 6) {
			throw new IllegalArgumentException(
					"Saturday " + date + " is not a weekday");
		}
		if (dow == 0) {
			throw new IllegalArgumentException(
					"Sunday " + date + " is not a weekday");
		}
		return last(date.plusDays(-1));
	}
	
	/**
	 * Add a number of weekdays (only accepts weekday arguments).
	 * 
	 */
	public TDate add(final TDate date, final int weekdays) {
		final int dow = date.getDayOfWeek();
		if (dow == 6) {
			throw new IllegalArgumentException(
					"Saturday " + date + " is not a weekday");
		}
		if (dow == 0) {
			throw new IllegalArgumentException(
					"Sunday " + date + " is not a weekday");
		}
		if (weekdays > 5) {
			final int fullWeeks = (int)Math.floor(weekdays/5.0);
			final int remaining = weekdays - fullWeeks * 5;
			return add(date.plusWeeks(fullWeeks), remaining);
		} else if (weekdays < -5) {
			final int fullWeeks = (int)Math.ceil(weekdays/5.0);
			final int remaining = weekdays - fullWeeks * 5;
			return add(date.plusWeeks(fullWeeks), remaining);
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
