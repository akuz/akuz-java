package me.akuz.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date formatting utility class.
 *
 */
public final class DateFmt {

	public static TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");
	public static String YYYYMMDD = "yyyyMMdd";
	public static String YYYYMMDD_dashed = "yyyy-MM-dd";
	public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	public static String StandardUtcFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static String ShortDateTimeFormat = "MMM dd, HH:mm z";

	public static final String format(Date date, String format) {
		return format(date, format, UTC_TIMEZONE);
	}

	public static final String format(Date date, String format, TimeZone timeZone) {
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		fmt.setTimeZone(timeZone);
		return fmt.format(date);
	}
	
	public static final Date parse(String dateStr, String format) throws ParseException {
		return parse(dateStr, format, UTC_TIMEZONE);
	}

	public static final Date parse(String dateStr, String format, TimeZone timeZone) throws ParseException {
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		fmt.setTimeZone(timeZone);
		return fmt.parse(dateStr);
	}
}
