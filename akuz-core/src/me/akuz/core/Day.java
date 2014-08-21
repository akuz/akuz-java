package me.akuz.core;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public final class Day {
	
	private final Date _date;
	
	public Day(final String str) throws ParseException {
		_date = DateFmt.parse(str, DateFmt.YYYYMMDD, DateFmt.UTC_TIMEZONE);
	}
	
	public Day(final int num) throws ParseException {
		if (num < 0) {
			throw new IllegalArgumentException("Day number representation cannot be negative");
		}
		int remaining = num;
		final int year = (int)Math.floor(num / 10000.0);
		
		remaining -= year * 10000;
		final int month = (int)Math.floor(remaining / 100.0);
		
		remaining -= month * 100;
		final int day = remaining;
		
		Calendar cal = Calendar.getInstance(DateFmt.UTC_TIMEZONE);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		_date = cal.getTime();
	}
	
	public int getYear() {
		// TODO
		return 0;
	}
	
	public Date getDateUTC() {
		return _date;
	}
	
	

}
