package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class RepeatWithoutExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private final Object _defaultValue;
	private final List<TItem<T>> _movedItems;
	private TItem<T> _currItem;
	private T _currTime;
	
	public RepeatWithoutExpiry() {
		this(null);
	}
	
	public RepeatWithoutExpiry(Object defaultValue) {
		_defaultValue = defaultValue;
		_movedItems = new ArrayList<>(1);
	}

	@Override
	public void next(
			final TLog<T> log,
			final T time,
			final SeqCursor<T> iter) {
		
		CurrTime.checkNew(_currTime, time);
		
		// update last item
		final List<TItem<T>> movedItems = iter.getMovedItems();
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

	@Override
	public TItem<T> getCurrItem() {
		CurrTime.checkSet(_currTime);
		return _currItem;
	}

	@Override
	public List<TItem<T>> getMovedItems() {
		CurrTime.checkSet(_currTime);
		return _movedItems;
	}

}
