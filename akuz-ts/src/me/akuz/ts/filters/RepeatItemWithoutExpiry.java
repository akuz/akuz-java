package me.akuz.ts.filters;

import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class RepeatItemWithoutExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private TItem<T> _currFilterItem;
	private T _currTime;
	
	public RepeatItemWithoutExpiry() {
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
			_currFilterItem = new TItem<T>(time, lastMovedItem);
		}
		
		_currTime = time;
	}

	@Override
	public TItem<T> getCurrItem() {
		CurrTime.checkSet(_currTime);
		return _currFilterItem;
	}

	@Override
	public List<TItem<T>> getMovedItems() {
		CurrTime.checkSet(_currTime);
		return null;
	}

}
