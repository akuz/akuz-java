package me.akuz.core;

import org.joda.time.LocalDate;

/**
 * Represents a date in some local (unknown) time zone.
 * 
 * Had to write a wrapper around Joda LocalDate, because LocalDate
 * does not implement Comparable<LocalDate> which is needed by the 
 * akuz-ts library.
 *
 */
public final class TDate implements Comparable<TDate> {
	
	private final LocalDate _date;
	
	public TDate(final LocalDate date) {
		_date = date;
	}
	
	public TDate(final String str) {
		_date = LocalDate.parse(str);
	}
	
	public TDate(final int num) {
		if (num < 0) {
			throw new IllegalArgumentException("DateOnly integer representation cannot be negative");
		}
		int remaining = num;
		final int year = (int)Math.floor(remaining / 10000.0);
		
		remaining -= year * 10000;
		final int month = (int)Math.floor(remaining / 100.0);
		
		remaining -= month * 100;
		final int day = remaining;
		
		_date = new LocalDate(year, month, day);
	}
	
	public int getNum() {
		return 
				_date.getYear() * 10000 + 
				_date.getMonthOfYear() * 100 + 
				_date.getDayOfMonth();
	}
	
	public LocalDate get() {
		return _date;
	}
	
	public TDate plusYears(final int years) {
		return new TDate(_date.plusYears(years));
	}
	
	public TDate plusMonths(final int months) {
		return new TDate(_date.plusMonths(months));
	}
	
	public TDate plusWeeks(final int weeks) {
		return new TDate(_date.plusWeeks(weeks));
	}
	
	public TDate plusDays(final int days) {
		return new TDate(_date.plusDays(days));
	}
	
	public int getDayOfWeek() {
		return _date.getDayOfWeek();
	}
	
	public int getDayOfMonth() {
		return _date.getDayOfMonth();
	}
	
	public int getDayOfYear() {
		return _date.getDayOfYear();
	}

	@Override
	public int compareTo(TDate o) {
		return _date.compareTo(o._date);
	}
	
	@Override
	public String toString() {
		return _date.toString();
	}
	
	@Override
	public int hashCode() {
		return _date.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		return _date.equals(((TDate)obj)._date);
	}

}
