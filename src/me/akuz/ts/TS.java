package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single value time series.
 *
 * @param <T> - Time type.
 */
public final class TS<T extends Comparable<T>> {
	
	private final List<TSItem<T>> _items;
	private final List<TSItem<T>> _itemsReadOnly;
	
	public TS() {
		_items = new ArrayList<>();
		_itemsReadOnly = Collections.unmodifiableList(_items);
	}

	public void add(TSItem<T> entry) {
		int lastTimeCmp = -1;
		if (_items.size() > 0) {
			TSItem<T> lastItem = _items.get(_items.size()-1);
			lastTimeCmp = lastItem.getTime().compareTo(entry.getTime());
		}
		if (lastTimeCmp > 0) {
			throw new IllegalStateException("Time series values must be added in chronological order");
		}
		if (lastTimeCmp == 0) {
			// remove last entry with the same time
			_items.remove(_items.size()-1);
		}
		_items.add(entry);
	}
	
	public TSItem<T> getLast() {
		return _items.size() > 0 ? _items.get(_items.size()-1) : null;
	}

	public void removeLast() {
		if (_items.size() > 0) {
			_items.remove(_items.size()-1);
		}
	}
	
	public List<TSItem<T>> getItems() {
		return _itemsReadOnly;
	}

}
