package me.akuz.core;

import java.util.Date;

/**
 * Time period.
 *
 */
public final class Period implements Comparable<Period> {

	private static final long MS_IN_SECOND = 1000;
	private static final long MS_IN_MINUTE = 60 * MS_IN_SECOND;
	private static final long MS_IN_HOUR   = 60 * MS_IN_MINUTE;
	private static final long MS_IN_DAY    = 24 * MS_IN_HOUR;
	private final long _ms;
	
	public Period(long ms) {
		_ms = ms;
	}
	
	public long getMs() {
		return _ms;
	}
	
	public final double getDays() {
		return (double)_ms / (double)MS_IN_DAY;
	}
	
	public final double getHours() {
		return (double)_ms / (double)MS_IN_HOUR;
	}
	
	public final double getMinutes() {
		return (double)_ms / (double)MS_IN_MINUTE;
	}
	
	public final double getSeconds() {
		return (double)_ms / (double)MS_IN_SECOND;
	}
	
	public static final Period fromDays(double days) {
		return new Period((long)(days * MS_IN_DAY));
	}

	public static final Period fromHours(double hours) {
		return new Period((long)(hours * MS_IN_HOUR));
	}

	public static final Period fromMinutes(double minutes) {
		return new Period((long)(minutes * MS_IN_MINUTE));
	}

	public static final Period fromSeconds(double seconds) {
		return new Period((long)(seconds * MS_IN_SECOND));
	}
	
	public static final Period fromMillis(long ms) {
		return new Period(ms);
	}
	
	public static final Period dateMinus(Date date1, Date date2) {
		return new Period(date1.getTime() - date2.getTime());
	}

	@Override
	public int compareTo(Period o) {
		if (_ms > o._ms) {
			return 1;
		} else if (_ms < o._ms) {
			return -1;
		} else {
			return 0;
		}
	}
	
	@Override
	public String toString() {
		return _ms + "ms";
	}

}
