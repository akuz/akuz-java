package me.akuz.core;

public final class Hit implements Comparable<Hit> {
	
	public final static Hit Empty = new Hit(0, 0);
	
	private final int _start;
	private final int _end;
	
	public Hit(final String str) {
		this(0, str.length());
	}
	
	public Hit(final int start, final int end) {
		if (start > end) {
			throw new IllegalArgumentException("Hit must have non-negative width; specified: [" + start + ", " + end + ")");
		}
		_start = start;
		_end = end;
	}
	
	public final int start() {
		return _start;
	}
	
	public final int end() {
		return _end;
	}
	
	public Hit shift(int offset) {
		return new Hit(_start + offset, _end + offset);
	}

	public final boolean overlaps(Hit other) {
		if (_start >= other._end) {
			return false;
		}
		if (_end <= other._start) {
			return false;
		}
		return true;
	}

	public final boolean includes(Hit other) {
		return 
			_start <= other._start && 
			_end >= other._end;
	}
	
	public final int length() {
		return _end - _start;
	}
	
	public int compareTo(Hit o) {
		if (_start < o._start) {
			return -1;
		} else if (_start > o._start) {
			return 1;
		} else if (_end > o._end) {
			return -1;
		} else if (_end < o._end) {
			return 1;
		} else {
			return 0;
		}
	}

	public int distanceTo(Hit other) {
		if (_start >= other._end) {
			return _start - other._end;
		}
		if (other._start >= _end) {
			return other._start - _end;
		}
		return 0; // overlaps
	}
	
	@Override
	public int hashCode() {
		return _start + 17 * _end;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		Hit other = (Hit)obj;
		return _start == other._start 
			&& _end   == other._end;
	}
	
	@Override
	public String toString() {
		return String.format("[%d,%d)", _start, _end);
	}
}
