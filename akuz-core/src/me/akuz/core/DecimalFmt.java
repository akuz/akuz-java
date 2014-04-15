package me.akuz.core;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Decimal formatting utility class.
 *
 */
public final class DecimalFmt {
	
	public static final String ZeroSpacePlus8D = "' '0.00000000;'-'0.00000000";

	private static final ThreadLocal<Map<String, DecimalFormat>> _threadLocal = new ThreadLocal<>();

	public static final String formatZeroSpacePlus8D(final double number) {
		return format(number, ZeroSpacePlus8D);
	}

	public static final String format(final double number, final String format) {
		Map<String, DecimalFormat> map = _threadLocal.get();
		if (map == null) {
			map = new HashMap<>();
			_threadLocal.set(map);
		}
		DecimalFormat fmt = map.get(format);
		if (fmt == null) {
			fmt = new DecimalFormat(format);
			map.put(format, fmt);
		}
		return fmt.format(number);
	}
	
	
}
