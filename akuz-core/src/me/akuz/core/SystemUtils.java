package me.akuz.core;

public final class SystemUtils {
	
	private static final String _lineSeparator = System.getProperty("line.separator");

	public static boolean isLocalhost() {
		String isLocalhost = System.getProperty("isLocalhost");
		return isLocalhost != null && isLocalhost.length() > 0;
	}
	
	public static String lineSeparator() {
		return _lineSeparator;
	}
}
