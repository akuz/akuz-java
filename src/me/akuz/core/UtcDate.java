package me.akuz.core;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class UtcDate implements Comparable<UtcDate> {

	public static String UTC = "UTC";
	public static String NumbersDateOnlyFormatString = "yyyyMMdd";
	public static String NumbersDateAndTimeFormatString = "yyyyMMddHHmmss";
	public static String StandardUtcFormatString = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static String ShortDateFormatString = "MMM dd, HH:mm z";
	public static SimpleDateFormat StandardUtcFormat;

	static {
		StandardUtcFormat = new SimpleDateFormat(StandardUtcFormatString);
		StandardUtcFormat.setTimeZone(TimeZone.getTimeZone(UTC));
	}
	
	private final Calendar _cal;
	private final SimpleDateFormat _customFormat;
	
	public UtcDate() {
		this(null, null, UTC);
	}
	
	public UtcDate(Date date) {
		this(date, null, UTC);
	}
	
	public UtcDate(String customFormatString) {
		this(null, customFormatString, UTC);
	}
	
	public UtcDate(Date date, String customFormatString) {
		this(date, customFormatString, UTC);
	}

	public UtcDate(Date date, String customFormatString, String timezoneName) {
		_cal = Calendar.getInstance(TimeZone.getTimeZone(timezoneName));
		if (date != null) {
			_cal.setTime(date);
		}
		if (customFormatString != null) {
			_customFormat = new SimpleDateFormat(customFormatString);
			_customFormat.setTimeZone(TimeZone.getTimeZone(timezoneName));
		} else {
			_customFormat = null;
		}
	}
	
	public UtcDate set(Date date) {
		_cal.setTime(date);
		return this;
	}
	
	public UtcDate add(int period, int number) {
		_cal.add(period, number);
		return this;
	}
	
	public Calendar getCal() {
		return _cal;
	}
	
	public Date getDate() {
		return _cal.getTime();
	}
	
	public long getTime() {
		return _cal.getTimeInMillis();
	}
	
	public Calendar parse(String str) throws ParseException {
		if (_customFormat == null) {
			throw new IllegalStateException("Cannot parse " + getClass().getSimpleName() + " - no format provided on creation.");
		}
		_cal.setTime(_customFormat.parse(str));
		return _cal;
	}
	
	public static final Date parse(String str, String format, String timeZone) throws ParseException {
		DateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(TimeZone.getTimeZone(timeZone));
		return df.parse(str);
	}
	
	public int compareTo(UtcDate other) {
		return _cal.compareTo(other._cal);
	}
	
	public String toString() {
		if (_customFormat == null) {
			return StandardUtcFormat.format(_cal.getTime());
		} else {
			return _customFormat.format(_cal.getTime());
		}
	}
	
	public UtcDate removeTime() {
		_cal.set(Calendar.MILLISECOND, 0);
		_cal.set(Calendar.SECOND, 0);
		_cal.set(Calendar.MINUTE, 0);
		_cal.set(Calendar.HOUR_OF_DAY, 0);
		return this;
	}
	
	public UtcDate removeMinutes() {
		_cal.set(Calendar.MILLISECOND, 0);
		_cal.set(Calendar.SECOND, 0);
		_cal.set(Calendar.MINUTE, 0);
		return this;
	}
}
