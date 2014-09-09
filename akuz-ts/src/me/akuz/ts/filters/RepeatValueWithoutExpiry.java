package me.akuz.ts.filters;

import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class RepeatValueWithoutExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private final Object _defaultValue;
	
	public RepeatValueWithoutExpiry() {
		this(null);
	}
	
	public RepeatValueWithoutExpiry(Object defaultValue) {
		_defaultValue = defaultValue;
	}

	@Override
	public void next(
			final T time,
			final SeqCursor<T> cur,
			final TLog<T> log) {
		
		CurrTime.checkNew(_currTime, time);
		
		// update last item
		final List<TItem<T>> movedItems = cur.getMovedItems();
		if (movedItems.size() > 0) {
			final TItem<T> lastMovedItem = movedItems.get(movedItems.size()-1);
			_currItem = new TItem<T>(time, lastMovedItem.getObject());
		} else if (_defaultValue != null) {
			_currItem = new TItem<T>(time, _defaultValue);
		}
		
		_movedItems.clear();
		if (_currItem != null) {
			_movedItems.add(_currItem);
		}
		
		_currTime = time;
	}

}
