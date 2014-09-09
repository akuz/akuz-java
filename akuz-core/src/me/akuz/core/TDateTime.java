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
public final class TDateTime implements Comparable<TDateTime> {
	
	private final DateTime _dateTime;
	
	public static final TDateTime parse(final String str) {
		return TDateTime.from(DateTime.parse(str));
	}
	
	public static final TDateTime from(final DateTime dateTime) {
		return new TDateTime(dateTime);
	}
	
	public TDateTime(
			final int year,
			final int monthOfYear,
			final int dayOfMonth) {
		
		_dateTime = new DateTime(
				year,
				monthOfYear,
				dayOfMonth,
				0,
				0);
	}
	
	public TDateTime(
			final int year,
			final int monthOfYear,
			final int dayOfMonth,
			final int hourOfDay,
			int minuteOfHour) {
		
		_dateTime = new DateTime(
				year,
				monthOfYear,
				dayOfMonth,
				hourOfDay,
				minuteOfHour);
	}
	
	public TDateTime(final DateTime dateTime) {
		_dateTime = dateTime;
	}
	
	public DateTime get() {
		return _dateTime;
	}
	
	public TDate toLocalDate() {
		return new TDate(_dateTime.toLocalDate());
	}

	@Override
	public int compareTo(TDateTime o) {
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
		return _dateTime.equals(((TDateTime)obj)._dateTime);
	}

}
