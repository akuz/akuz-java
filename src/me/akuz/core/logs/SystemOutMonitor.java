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
		System.out.println(
				DateFmt.format(new Date(), DateFmt.NumbersDateTimeFormat) 
				+ ": " + message);
	}

	@Override
	public void write(String message, Throwable ex) {
		System.out.println(
				DateFmt.format(new Date(), DateFmt.NumbersDateTimeFormat) 
				+ ": " + message);
		ex.printStackTrace(System.out);
	}

}
