package me.akuz.ts.filters.stats;

import java.util.List;

import me.akuz.ts.TItem;
import me.akuz.ts.filters.Filter;
import me.akuz.ts.log.TLog;

/**
 * Cumsum filter (1D).
 *
 */
public class Cumsum<T extends Comparable<T>> extends Filter<T> {
	
	private final double _startValue;
	private TItem<T> _currFilteredItem;
	
	public Cumsum() {
		this(0.0);
	}
	
	public Cumsum(final double startValue) {
		_startValue = startValue;
	}

	@Override
	public void next(
			TLog log,
			T currTime,
			TItem<T> currItem,
			List<TItem<T>> movedItems) {
		
		// get current value
		double currValue = _startValue;
		if (_currFilteredItem != null) {
			currValue = _currFilteredItem.getDouble();
		}
		
		// add all moved items
		for (int i=0; i<movedItems.size(); i++) {
			currValue += movedItems.get(i).getNumber().doubleValue();
		}
		
		// set new current item
		_currFilteredItem = new TItem<T>(currTime, currValue);
	}

	@Override
	public TItem<T> getCurrItem() {
		return _currFilteredItem;
	}

}
