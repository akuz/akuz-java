package me.akuz.qf.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Time series.
 *
 * @param <T> - Time type, must be comparable to each other.
 */
public class TS<T extends Comparable<T>> {

	private final List<TSEntry<T>> _sortQueue;
	private final List<TSEntry<T>> _sorted;
	private final List<TSEntry<T>> _sortedReadOnly;
	
	public TS() {
		_sortQueue = new ArrayList<>();
		_sorted = new ArrayList<>();
		_sortedReadOnly = Collections.unmodifiableList(_sorted);
	}
	
	public void add(TSEntry<T> entry) {
		_sortQueue.add(entry);
	}
	
	public void sort() {
		if (_sortQueue.size() > 0) {
			_sorted.addAll(_sortQueue);
			_sortQueue.clear();
			if (_sorted.size() > 1) {
				Collections.sort(_sorted);
			}
		}
	}
	
	public List<TSEntry<T>> getSorted() {
		if (_sortQueue.size() > 0) {
			sort();
		}
		return _sortedReadOnly;
	}
}
