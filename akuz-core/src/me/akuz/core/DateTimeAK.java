package me.akuz.core;

import org.joda.time.DateTime;

/**
 * Represents a date/time in some local time zone.
 * 
 * Had to write a wrapper around Joda DateTime, because DateTime
 * does not implement Comparable<DateTime> which is needed by the 
 * akuz-ts library.
 *
 */
public final class DateTimeAK implements Comparable<DateTimeAK> {
	
	private final DateTime _dateTime;
	
	public static final DateTimeAK parse(final String str) {
		return DateTimeAK.from(DateTime.parse(str));
	}
	
	public static final DateTimeAK from(final DateTime dateTime) {
		return new DateTimeAK(dateTime);
	}
	
	public DateTimeAK(final DateTime dateTime) {
		_dateTime = dateTime;
	}
	
	public DateTime get() {
		return _dateTime;
	}

	@Override
	public int compareTo(DateTimeAK o) {
		return _dateTime.compareTo(o._dateTime);
	}
	
	@Override
	public String toString() {
		return _dateTime.toString();
	}
	
	@Override
	public int hashCode() {
		return _dateTime.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		return _dateTime.equals(((DateTimeAK)obj)._dateTime);
	}

}
