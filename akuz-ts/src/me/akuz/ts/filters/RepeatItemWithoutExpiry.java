package me.akuz.ts.filters;

import java.util.List;

import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class RepeatItemWithoutExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private TItem<T> _currFilterItem;
	
	public RepeatItemWithoutExpiry() {
	}
	
	@Override
	public void next(
			final TLog<T> log,
			final T currTime,
			final SeqIterator<T> iter) {
		
		// update last item
		final List<TItem<T>> movedItems = iter.getMovedItems();
		if (movedItems.size() > 0) {
			final TItem<T> lastMovedItem = movedItems.get(movedItems.size()-1);
			_currFilterItem = new TItem<T>(currTime, lastMovedItem);
		}
	}

	@Override
	public TItem<T> getCurrItem() {
		return _currFilterItem;
	}

}
