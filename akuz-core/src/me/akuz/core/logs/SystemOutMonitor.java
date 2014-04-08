package me.akuz.core.logs;

import java.util.Date;

import me.akuz.core.DateFmt;

/**
 * Writes monitoring information to System.out.
 *
 */
public final class SystemOutMonitor implements Monitor {

	@Override
	public void write(String message) {
		write(message, null);
	}

	@Override
	public void write(String message, Throwable ex) {
		System.out.println(
				DateFmt.format(new Date(), DateFmt.YYYYMMDDHHMMSS) 
				+ ": " + message);
		if (ex != null) {
			ex.printStackTrace(System.out);
		}
	}

}
