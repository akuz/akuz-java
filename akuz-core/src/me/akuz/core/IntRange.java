package me.akuz.core;

import java.io.Serializable;

public final class IntRange implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int _start;
	private int _end;
	
	public IntRange() {
		// required for deserialization
	}
	
	public IntRange(int start, int end) {
		_start = start;
		_end = end;
	}
	
	public int getStart() {
		return _start;
	}
	
	public int getEnd() {
		return _end;
	}
}
