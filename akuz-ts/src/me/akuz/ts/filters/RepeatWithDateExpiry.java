package me.akuz.ts.filters;

import java.util.List;

import me.akuz.core.TDate;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

import org.joda.time.Days;

public final class RepeatWithDateExpiry extends Filter<TDate> {
	
	private final Days _aliveDays;
	private final Object _defaultValue;
	private TItem<TDate> _lastAvailableItem;
	private TItem<TDate> _currFilterItem;
	
	public RepeatWithDateExpiry(final Days aliveDays) {
		this(aliveDays, null);
	}
	
	public RepeatWithDateExpiry(final Days aliveDays, Object defaultValue) {
		_aliveDays = aliveDays;
		_defaultValue = defaultValue;
	}

	@Override
	public void next(
			final TLog<TDate> log,
			final TDate currTime,
			final SeqIterator<TDate> iter) {
		
		// update last item
		final List<TItem<TDate>> movedItems = iter.getMovedItems();
		if (movedItems.size() > 0) {
			_lastAvailableItem = movedItems.get(movedItems.size()-1);
		}
		
		// check if expired
		if (_lastAvailableItem != null) {
			
			final Days diff = Days.daysBetween(_lastAvailableItem.getTime().get(), currTime.get());
			
			if (diff.getDays() < 0) {
				throw new IllegalStateException(
						"Times are not in chronological order in \"" + getFieldName() + 
						"\"; last known time: " + _lastAvailableItem.getTime() + "; new time: " + currTime);
			}
			
			// set last item
			if (diff.compareTo(_aliveDays) <= 0) {
				_currFilterItem = new TItem<TDate>(currTime, _lastAvailableItem.getObject());
			} else if (_defaultValue != null) {
				_currFilterItem = new TItem<TDate>(currTime, _defaultValue);
			} else {
				_currFilterItem = null;
			}
		}
	}

	@Override
	public TItem<TDate> getCurrItem() {
		return _currFilterItem;
	}

}