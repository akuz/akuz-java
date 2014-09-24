package me.akuz.core;

import org.joda.time.Days;

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
		if (weekdays <= -5 || 5 <= weekdays) {
			final int fullWeeks = weekdays / 5;
			final int weekdaysRemaining = weekdays % 5;
			final TDate dateAfterWeeks = date.plusWeeks(fullWeeks);
			return weekdaysRemaining == 0 ? dateAfterWeeks : add(dateAfterWeeks, weekdaysRemaining);
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
	
	/**
	 * Calculate the distance in weekdays from one date 
	 * to another, where both dates must be weekdays.
	 * 
	 */
	public static int distance(final TDate date1, final TDate date2) {
		checkWeekday(date1);
		checkWeekday(date2);
		final int days = Days.daysBetween(
				date1.get(), 
				date2.get())
				.getDays();
		final int fullWeeks = days / 7;
		final int daysRemaining = days % 7;
		final int weekdaysAfterWeeks = fullWeeks * 5;
		if (daysRemaining == 0) {
			return weekdaysAfterWeeks;
		}
		final int step = daysRemaining < 0 ? -1 : 1;
		TDate date = add(date1, weekdaysAfterWeeks);
		int weekdaysRemaining = 0;
		while (!date.equals(date2)) {
			weekdaysRemaining += step;
			date = add(date, step);
		}
		return weekdaysAfterWeeks + weekdaysRemaining;
	}
	
	/**
	 * Calculate *forward* distance in weekdays from one date 
	 * to another, where both dates must be weekdays, returning
	 * zero if the distance is in fact negative.
	 * 
	 */
	public static int forwardDistance(final TDate date1, final TDate date2) {
		final int distance = distance(date1, date2);
		return distance > 0 ? distance : 0;
	}
}
