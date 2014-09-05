package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class RepeatItemWithoutExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private final List<TItem<T>> _movedItems;
	private TItem<T> _currItem;
	private T _currTime;
	
	public RepeatItemWithoutExpiry() {
		_movedItems = new ArrayList<>(1);
	}
	
	@Override
	public void next(
			final T time,
			final SeqCursor<T> cur,
			final TLog<T> log) {
		
		CurrTime.checkNew(_currTime, time);
		
		// update last item
		final List<TItem<T>> movedItems = cur.getMovedItems();
		if (movedItems != null && movedItems.size() > 0) {
			final TItem<T> lastMovedItem = movedItems.get(movedItems.size()-1);
			_currItem = new TItem<T>(time, lastMovedItem);
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
