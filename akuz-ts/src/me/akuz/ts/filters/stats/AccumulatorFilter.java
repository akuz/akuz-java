package me.akuz.ts.filters.stats;

import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.filters.stats.accs.Accumulator;
import me.akuz.ts.log.TLog;

/**
 * Accumulator filter (1D).
 *
 */
public class AccumulatorFilter<T extends Comparable<T>> extends Filter<T> {
	
	private Accumulator<T> _accumulator;
	
	public AccumulatorFilter(final Accumulator<T> accumulator) {
		if (accumulator == null) {
			throw new IllegalArgumentException("Accumulator cannot be null");
		}
		_accumulator = accumulator;
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
	public Filter<T> clone() {
		AccumulatorFilter<T> copy = (AccumulatorFilter<T>)super.clone();
		copy._accumulator = _accumulator.clone();
		return copy;
	}

}
