package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single value time series.
 *
 * @param <T> - Time type.
 */
public class TS<T extends Comparable<T>> {
	
	private final List<TSItem<T>> _items;
	private final List<TSItem<T>> _itemsReadOnly;
	private TSItem<T> _lastEntry;
	
	public TS() {
		_items = new ArrayList<>();
		_itemsReadOnly = Collections.unmodifiableList(_items);
	}

	public void add(TSItem<T> entry) {
		int lastTimeCmp = -1;
		if (_lastEntry != null) {
			lastTimeCmp = _lastEntry.getTime().compareTo(entry.getTime());
		}
		if (lastTimeCmp > 0) {
			throw new IllegalStateException("Values must be added in chronological order");
		}
		if (lastTimeCmp == 0) {
			// remove last entry with the same time
			_items.remove(_items.size()-1);
		}
		_items.add(entry);
		_lastEntry = entry;
	}
	
	public List<TSItem<T>> getItems() {
		return _itemsReadOnly;
	}

}
