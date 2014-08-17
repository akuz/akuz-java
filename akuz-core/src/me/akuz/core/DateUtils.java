package me.akuz.core;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtils {

	private final static long MS_in_SEC  = 1000;
	private final static long MS_in_MIN  = 60 * MS_in_SEC;
	private final static long MS_in_HOUR = 60 * MS_in_MIN;
	private final static long MS_in_DAY  = 24 * MS_in_HOUR;
	private final static long MS_in_WEEK = 7  * MS_in_DAY;

	public static Calendar getUTCCalendar() {
		final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		return cal;
	}

	public static Date addDays(Date date, double days) {
		
		final long newTime = date.getTime() + (long)(MS_in_DAY * days);
		final Date newDate = (Date)date.clone();
		newDate.setTime(newTime);
		return newDate;
	}

	public static Date addHours(Date date, double hours) {
		
		final long newTime = date.getTime() + (long)(MS_in_HOUR * hours);
		final Date newDate = (Date)date.clone();
		newDate.setTime(newTime);
		return newDate;
	}

	public static Date addMinutes(Date date, double mins) {
		
		final long newTime = date.getTime() + (long)(MS_in_MIN * mins);
		final Date newDate = (Date)date.clone();
		newDate.setTime(newTime);
		return newDate;
	}

	public static Date addSeconds(Date date, double seconds) {
		
		final long newTime = date.getTime() + (long)(MS_in_SEC * seconds);
		final Date newDate = (Date)date.clone();
		newDate.setTime(newTime);
		return newDate;
	}

	public static Date addMs(Date date, long ms) {
		
		final long newTime = date.getTime() + ms;
		final Date newDate = (Date)date.clone();
		newDate.setTime(newTime);
		return newDate;
	}

	public static double daysBetween(Date date1, Date date2) {
		
		// take times of observations in milliseconds
		long longHistTime1 = date1.getTime();
		long longHistTime2 = date2.getTime();
		
		// calculate the difference between dates in days
		double interval = ((double)(longHistTime2 - longHistTime1)) / MS_in_DAY;
		
		return interval;
	}

	public static double hoursBetween(Date date1, Date date2) {
		
		// take times of observations in milliseconds
		long longHistTime1 = date1.getTime();
		long longHistTime2 = date2.getTime();
		
		// calculate the difference between dates in days
		double interval = ((double)(longHistTime2 - longHistTime1)) / MS_in_HOUR;
		
		return interval;
	}

	public static double minutesBetween(Date date1, Date date2) {
		
		// take times of observations in milliseconds
		long longHistTime1 = date1.getTime();
		long longHistTime2 = date2.getTime();
		
		// calculate the difference between dates in days
		double interval = ((double)(longHistTime2 - longHistTime1)) / MS_in_MIN;
		
		return interval;
	}

	public static double secondsBetween(Date date1, Date date2) {
		
		// take times of observations in milliseconds
		long longHistTime1 = date1.getTime();
		long longHistTime2 = date2.getTime();
		
		// calculate the difference between dates in days
		double interval = ((double)(longHistTime2 - longHistTime1)) / MS_in_SEC;
		
		return interval;
	}

	public static long msBetween(Date date1, Date date2) {
		
		// take times of observations in milliseconds
		long longHistTime1 = date1.getTime();
		long longHistTime2 = date2.getTime();
		
		// calculate the difference between dates in days
		long interval = longHistTime2 - longHistTime1;
		
		return interval;
	}

	public static Date removeTimeUTC(Date date) {
		return (new UtcDate(date)).removeTime().getDate();
	}	

	public static Date removeMinutesUTC(Date date) {
		return (new UtcDate(date)).removeMinutes().getDate();
	}

	public final static String formatAgo(Date date, Date agoFrom) {
		String agoStr = null;
		
		long ms = agoFrom.getTime() - date.getTime();
		if (ms < 0) {
			agoStr = "Just Now";
		} else {
			
			
			if (ms >= MS_in_WEEK) {
				
				long num_weeks = ms / MS_in_WEEK;
				
				agoStr = String.format("%d week%s ago", 
						num_weeks, num_weeks > 1 ? "s" : "");
				
			} else if (ms >= MS_in_DAY) {
				
				long num_days = ms / MS_in_DAY;
				
				agoStr = String.format("%d day%s ago", 
						num_days, num_days > 1 ? "s" : "");
				
			} else if (ms >= MS_in_HOUR) {
				
				long num_hours = ms / MS_in_HOUR;
				
				agoStr = String.format("%d hour%s ago", 
						num_hours, num_hours > 1 ? "s" : "");
				
			} else if (ms >= MS_in_MIN) {
				
				long num_mins = ms / MS_in_MIN;
	
				agoStr = String.format("%d minute%s ago", 
						num_mins, num_mins > 1 ? "s" : "");
				
			} else if (ms >= MS_in_SEC) {
				
				long num_secs = ms / MS_in_SEC;
				
				agoStr = String.format("%d second%s ago", 
						num_secs, num_secs > 1 ? "s" : "");
			
			} else {
				
				agoStr = "Now";
			}
		}
		
		return agoStr;
	}	
	
}
