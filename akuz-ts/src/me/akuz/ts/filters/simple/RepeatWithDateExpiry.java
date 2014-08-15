package me.akuz.ts.filters.simple;

import java.util.Date;
import java.util.List;

import me.akuz.core.Period;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class RepeatWithDateExpiry extends Filter<Date> {
	
	private final Period _alivePeriod;
	private final Object _defaultValue;
	private TItem<Date> _lastAvailableItem;
	private TItem<Date> _currFilterItem;
	
	public RepeatWithDateExpiry(final Period alivePeriod) {
		this(alivePeriod, null);
	}
	
	public RepeatWithDateExpiry(final Period alivePeriod, Object defaultValue) {
		_alivePeriod = alivePeriod;
		_defaultValue = defaultValue;
	}

	@Override
	public void next(
			final TLog log,
			final Date currTime,
			final SeqIterator<Date> iter) {
		
		// update last item
		final List<TItem<Date>> movedItems = iter.getMovedItems();
		if (movedItems.size() > 0) {
			_lastAvailableItem = movedItems.get(movedItems.size()-1);
		}
		
		// check if expired
		if (_lastAvailableItem != null) {
			
			final long diff = currTime.getTime() - _lastAvailableItem.getTime().getTime();
			
			if (diff < 0) {
				throw new IllegalStateException(
						"Times are not in chronological order in \"" + getFieldName() + 
						"\"; last known time: " + _lastAvailableItem.getTime() + "; new time: " + currTime);
			}
			
			// set last item
			if (diff <= _alivePeriod.getMs()) {
				_currFilterItem = new TItem<Date>(currTime, _lastAvailableItem.getObject());
			} else if (_defaultValue != null) {
				_currFilterItem = new TItem<Date>(currTime, _defaultValue);
			} else {
				_currFilterItem = null;
			}
		}
	}

	@Override
	public TItem<Date> getCurrItem() {
		return _currFilterItem;
	}

}
