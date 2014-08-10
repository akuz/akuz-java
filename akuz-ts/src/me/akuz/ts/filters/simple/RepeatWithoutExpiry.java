package me.akuz.ts.filters.simple;

import java.util.List;

import me.akuz.ts.TItem;
import me.akuz.ts.filters.Filter;
import me.akuz.ts.log.TLog;

public final class RepeatWithoutExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private Object _defaultValue;
	private TItem<T> _currFilterItem;
	
	public RepeatWithoutExpiry() {
		this(null);
	}
	
	public RepeatWithoutExpiry(Object defaultValue) {
		_defaultValue = defaultValue;
	}

	@Override
	public void next(
			final TLog log,
			final T currTime,
			final TItem<T> currItem,
			final List<TItem<T>> movedItems) {
		
		// update last item
		if (movedItems.size() > 0) {
			final TItem<T> lastMovedItem = movedItems.get(movedItems.size()-1);
			_currFilterItem = new TItem<T>(currTime, lastMovedItem.getObject());
		} else if (_defaultValue != null) {
			_currFilterItem = new TItem<T>(currTime, _defaultValue);
		}
	}

	@Override
	public TItem<T> getCurrItem() {
		return _currFilterItem;
	}

}
