package me.akuz.ts.filters.stats;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.filters.stats.accs.Accumulator;
import me.akuz.ts.log.TLog;

/**
 * Accumulation filter (1D).
 *
 */
public class AccumulatorFilter<T extends Comparable<T>> extends Filter<T> {
	
	private final Accumulator<T> _accumulator;
	private final List<TItem<T>> _movedItems;
	private TItem<T> _currItem;
	private T _currTime;
	
	public AccumulatorFilter(final Accumulator<T> accumulator) {
		if (accumulator == null) {
			throw new IllegalArgumentException("Accumulator cannot be null");
		}
		_accumulator = accumulator;
		_movedItems = new ArrayList<>();
	}

	@Override
	public void next(
			final T time,
			final SeqCursor<T> cursor,
			final TLog<T> log) {
		
		CurrTime.checkNew(_currTime, time);
		
		final List<TItem<T>> movedItems = cursor.getMovedItems();
		for (int i=0; i<movedItems.size(); i++) {
			final TItem<T> item = movedItems.get(i);
			_accumulator.add(item.getTime(), item.getObject());
		}
		if (cursor.getCurrItem() == null) {
			_accumulator.add(time, null);
		}

		_currItem = null;
		_movedItems.clear();
		final Object value = _accumulator.get();
		if (value != null) {
			final TItem<T> accItem = new TItem<T>(time, value);
			_movedItems.add(accItem);
			_currItem = accItem;
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
