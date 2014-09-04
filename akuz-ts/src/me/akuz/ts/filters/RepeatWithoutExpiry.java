package me.akuz.ts.filters;

import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class RepeatWithoutExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private Object _defaultValue;
	private TItem<T> _currItem;
	private T _currTime;
	
	public RepeatWithoutExpiry() {
		this(null);
	}
	
	public RepeatWithoutExpiry(Object defaultValue) {
		_defaultValue = defaultValue;
	}

	@Override
	public void next(
			final TLog<T> log,
			final T time,
			final SeqIterator<T> iter) {
		
		CurrTime.checkNew(_currTime, time);
		
		// update last item
		final List<TItem<T>> movedItems = iter.getMovedItems();
		if (movedItems.size() > 0) {
			final TItem<T> lastMovedItem = movedItems.get(movedItems.size()-1);
			_currItem = new TItem<T>(time, lastMovedItem.getObject());
		} else if (_defaultValue != null) {
			_currItem = new TItem<T>(time, _defaultValue);
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
		return null;
	}

}
