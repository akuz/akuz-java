package me.akuz.ts.filters.stats;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

/**
 * Cumsum filter (1D).
 *
 */
public class CurrentCumsum<T extends Comparable<T>> extends Filter<T> {
	
	private final double _startValue;
	private final List<TItem<T>> _movedItems;
	private TItem<T> _currItem;
	private T _currTime;
	
	public CurrentCumsum() {
		this(0.0);
	}
	
	public CurrentCumsum(final double startValue) {
		_startValue = startValue;
		_movedItems = new ArrayList<>(1);
	}

	@Override
	public void next(
			final T time,
			final SeqCursor<T> cursor,
			final TLog<T> log) {
		
		CurrTime.checkNew(_currTime, time);
		
		// get current value
		double currValue = _startValue;
		if (_currItem != null) {
			currValue = _currItem.getDouble();
		}
		
		// add all moved items
		final List<TItem<T>> movedItems = cursor.getMovedItems();
		for (int i=0; i<movedItems.size(); i++) {
			currValue += movedItems.get(i).getNumber().doubleValue();
		}
		
		// set current item
		_currItem = new TItem<T>(time, currValue);

		// set moved items
		_movedItems.clear();
		_movedItems.add(_currItem);
		
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
