package me.akuz.core;

import java.util.Date;

import org.joda.time.Duration;

/**
 * Time duration.
 * 
 * Had to write a wrapper around Joda Duration
 * because Joda duration does not provide partial
 * duration in hours, days, etc.
 *
 */
public final class TDuration implements Comparable<TDuration> {

	private static final long MS_IN_SECOND = 1000;
	private static final long MS_IN_MINUTE = 60 * MS_IN_SECOND;
	private static final long MS_IN_HOUR   = 60 * MS_IN_MINUTE;
	private static final long MS_IN_DAY    = 24 * MS_IN_HOUR;
	private final Duration _dur;
	
	public TDuration(final long ms) {
		_dur = new Duration(ms);
	}
	
	public TDuration(final Duration dur) {
		_dur = dur;
	}
	
	public Duration get() {
		return _dur;
	}
	
	public long getMs() {
		return _dur.getMillis();
	}
	
	public final double getDays() {
		return (double)_dur.getMillis() / (double)MS_IN_DAY;
	}
	
	public final double getHours() {
		return (double)_dur.getMillis() / (double)MS_IN_HOUR;
	}
	
	public final double getMinutes() {
		return (double)_dur.getMillis() / (double)MS_IN_MINUTE;
	}
	
	public final double getSeconds() {
		return (double)_dur.getMillis() / (double)MS_IN_SECOND;
	}
	
	public static final TDuration fromDays(double days) {
		return new TDuration((long)(days * MS_IN_DAY));
	}

	public static final TDuration fromHours(double hours) {
		return new TDuration((long)(hours * MS_IN_HOUR));
	}

	public static final TDuration fromMinutes(double minutes) {
		return new TDuration((long)(minutes * MS_IN_MINUTE));
	}

	public static final TDuration fromSeconds(double seconds) {
		return new TDuration((long)(seconds * MS_IN_SECOND));
	}
	
	public static final TDuration fromMillis(long ms) {
		return new TDuration(ms);
	}
	
	public static final TDuration dateMinus(Date date1, Date date2) {
		return new TDuration(date1.getTime() - date2.getTime());
	}

	@Override
	public int compareTo(final TDuration o) {
		return _dur.compareTo(o.get());
	}
	
	@Override
	public int hashCode() {
		return _dur.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		return _dur.equals(((TDuration)obj)._dur);
	}
	
	@Override
	public String toString() {
		return _dur.toString();
	}

}
