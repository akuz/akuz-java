package me.akuz.ts.filters.stats;

import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

/**
 * Cumsum filter (1D).
 *
 */
public class CurrentCumsum<T extends Comparable<T>> extends Filter<T> {
	
	private final double _startValue;
	private TItem<T> _currItem;
	private T _currTime;
	
	public CurrentCumsum() {
		this(0.0);
	}
	
	public CurrentCumsum(final double startValue) {
		_startValue = startValue;
	}

	@Override
	public void next(
			final TLog<T> log,
			final T time,
			final SeqIterator<T> iter) {
		
		CurrTime.checkNew(_currTime, time);
		
		// get current value
		double currValue = _startValue;
		if (_currItem != null) {
			currValue = _currItem.getDouble();
		}
		
		// add all moved items
		final List<TItem<T>> movedItems = iter.getMovedItems();
		for (int i=0; i<movedItems.size(); i++) {
			currValue += movedItems.get(i).getNumber().doubleValue();
		}
		
		// set new current item
		_currItem = new TItem<T>(time, currValue);
		
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
