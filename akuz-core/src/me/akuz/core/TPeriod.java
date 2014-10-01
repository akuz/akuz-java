package me.akuz.core;

import java.util.Date;

import org.joda.time.Duration;

/**
 * Time period.
 * 
 * Had to write a wrapper around Joda Duration
 * because Joda duration does not provide partial
 * duration in hours, days, etc.
 *
 */
public final class TPeriod implements Comparable<TPeriod> {

	private static final long MS_IN_SECOND = 1000;
	private static final long MS_IN_MINUTE = 60 * MS_IN_SECOND;
	private static final long MS_IN_HOUR   = 60 * MS_IN_MINUTE;
	private static final long MS_IN_DAY    = 24 * MS_IN_HOUR;
	private final Duration _dur;
	
	public TPeriod(final long ms) {
		_dur = new Duration(ms);
	}
	
	public TPeriod(final Duration dur) {
		_dur = dur;
	}
	
	public TPeriod(final TDateTime dateTime1, final TDateTime dateTime2) {
		this(new Duration(dateTime1.get(), dateTime2.get()));
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
	
	public static final TPeriod fromDays(double days) {
		return new TPeriod((long)(days * MS_IN_DAY));
	}

	public static final TPeriod fromHours(double hours) {
		return new TPeriod((long)(hours * MS_IN_HOUR));
	}

	public static final TPeriod fromMinutes(double minutes) {
		return new TPeriod((long)(minutes * MS_IN_MINUTE));
	}

	public static final TPeriod fromSeconds(double seconds) {
		return new TPeriod((long)(seconds * MS_IN_SECOND));
	}
	
	public static final TPeriod fromMillis(long ms) {
		return new TPeriod(ms);
	}
	
	public static final TPeriod dateMinus(Date date1, Date date2) {
		return new TPeriod(date1.getTime() - date2.getTime());
	}

	@Override
	public int compareTo(final TPeriod o) {
		return _dur.compareTo(o._dur);
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
		return _dur.equals(((TPeriod)obj)._dur);
	}
	
	@Override
	public String toString() {
		return _dur.toString();
	}

}
