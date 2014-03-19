package me.akuz.qf.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Output time series.
 *
 * @param <T> - Time type.
 */
public final class TSOutput<T extends Comparable<T>> extends TS<T> {

	private final List<TSEntry<T>> _sorted;
	private final List<TSEntry<T>> _sortedReadOnly;
	private final boolean _allowDuplicateTimes;
	private T _lastTime;
	
	public TSOutput() {
		this(false);
	}
	
	public TSOutput(boolean allowDuplicateTimes) {
		_sorted = new ArrayList<>();
		_sortedReadOnly = Collections.unmodifiableList(_sorted);
		_allowDuplicateTimes = allowDuplicateTimes;
	}

	public void add(TSEntry<T> entry) {
		if (_lastTime != null) {
			if (_allowDuplicateTimes) {
				if (_lastTime.compareTo(entry.getTime()) > 0) {
					throw new IllegalStateException("Values must be added in chronological order (allowDuplicateTimes: " + _allowDuplicateTimes + ")");
				}
			} else {
				if (_lastTime.compareTo(entry.getTime()) >= 0) {
					throw new IllegalStateException("Values must be added in chronological order (allowDuplicateTimes: " + _allowDuplicateTimes + ")");
				}
			}
		}
		_sorted.add(entry);
		_lastTime = entry.getTime();
	}
	
	@Override
	public List<TSEntry<T>> getSorted() {
		return _sortedReadOnly;
	}

}
