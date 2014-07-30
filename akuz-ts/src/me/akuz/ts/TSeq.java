package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Time series sequence of values.
 *
 * @param <T> - Time type.
 */
public final class TSeq<T extends Comparable<T>> {
	
	private final TType _dataType;
	private final List<TItem<T>> _items;
	private final List<TItem<T>> _itemsReadOnly;
	private List<TItem<T>> _staged;
	private List<TItem<T>> _stagedReadOnly;
	
	public TSeq(TType dataType) {
		_dataType = dataType;
		_items = new ArrayList<>();
		_itemsReadOnly = Collections.unmodifiableList(_items);
	}
	
	public TType getDataType() {
		return _dataType;
	}
	
	public List<TItem<T>> getItems() {
		return _itemsReadOnly;
	}
	
	public TItem<T> getLast() {
		if (_items.size() > 0) {
			return _items.get(_items.size()-1);
		} else {
			return null;
		}
	}
	
	public void add(T time, Object value) {
		add(new TItem<>(time, value));
	}

	public void add(TItem<T> item) {
		int lastTimeCmp = -1;
		TItem<T> lastItem = null;
		if (_items.size() > 0) {
			lastItem = _items.get(_items.size()-1);
			lastTimeCmp = lastItem.getTime().compareTo(item.getTime());
		}
		if (lastTimeCmp >= 0) {
			throw new IllegalStateException("Time series values must be added in chronological order, " +
											"previous item time '" + lastItem.getTime() + "', " + 
											"added item time '" + item.getTime() + "'");
		}
		_items.add(item);
	}

	public void stage(T time, Object value) {
		stage(new TItem<>(time, value));
	}
	
	public void stage(TItem<T> item) {
		if (_staged == null) {
			_staged = new ArrayList<>();
			_stagedReadOnly = Collections.unmodifiableList(_staged);
		}
		_staged.add(item);
	}
	
	public List<TItem<T>> getStaged() {
		return _stagedReadOnly;
	}
	
	public void acceptStaged() {
		if (_staged == null ||
			_staged.size() == 0) {
			return;
		} else if (_staged.size() == 1) {
			add(_staged.get(0));
		} else {
			Collections.sort(_staged);
			for (int i=0; i<_staged.size(); i++) {
				add(_staged.get(i));
			}
		}
		_staged.clear();
	}
	
	public void clearStaged() {
		if (_staged != null) {
			_staged.clear();
		}
	}
	
	public void extractTimes(Set<T> times) {
		for (int i=0; i<_items.size(); i++) {
			times.add(_items.get(i).getTime());
		}
	}

}
