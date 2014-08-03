package me.akuz.core;

import java.util.Date;

public final class Interval implements Comparable<Interval> {
	
	private final Date _start;
	private final Date _end;
	
	public Interval(Date start, Date end) {
		if (start == null) {
			throw new IllegalArgumentException("Start cannot be null");
		}
		if (end == null) {
			throw new IllegalArgumentException("End cannot be null");
		}
		if (start.compareTo(end) > 0) {
			throw new IllegalArgumentException("Start must be <= end");
		}
		_start = start;
		_end = end;
	}
	public Date start() {
		return _start;
	}
	public Date end() {
		return _end;
	}
	@Override
	public int compareTo(Interval o) {
		return _start.compareTo(o._start);
	}
}
