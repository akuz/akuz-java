package me.akuz.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date formatting utility class.
 *
 */
public final class DateFmt {

	public static String UTC = "UTC";
	public static String NumbersDateFormat = "yyyyMMdd";
	public static String NumbersDateTimeFormat = "yyyyMMddHHmmss";
	public static String StandardUtcFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static String ShortDateTimeFormat = "MMM dd, HH:mm z";

	public static final String format(Date date, String format, String timeZone) {
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		fmt.setTimeZone(TimeZone.getTimeZone(timeZone));
		return fmt.format(date);
	}

	public static final String format(Date date, String format) {
		return format(date, format, UTC);
	}
}
