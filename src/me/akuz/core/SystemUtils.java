package me.akuz.core;

public final class SystemUtils {

	public static boolean isLocalhost() {
		String isLocalhost = System.getProperty("isLocalhost");
		return isLocalhost != null && isLocalhost.length() > 0;
	}
}
