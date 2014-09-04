package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.TDate;
import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

import org.joda.time.Days;

public final class RepeatWithDateExpiry extends Filter<TDate> {
	
	private final Days _aliveDays;
	private final Object _defaultValue;
	private TItem<TDate> _lastAvailableItem;
	private final List<TItem<TDate>> _movedItems;
	private TItem<TDate> _currItem;
	private TDate _currTime;
	
	public RepeatWithDateExpiry(final Days aliveDays) {
		this(aliveDays, null);
	}
	
	public RepeatWithDateExpiry(final Days aliveDays, Object defaultValue) {
		_aliveDays = aliveDays;
		_defaultValue = defaultValue;
		_movedItems = new ArrayList<>(1);
	}

	@Override
	public void next(
			final TLog<TDate> log,
			final TDate time,
			final SeqIterator<TDate> iter) {
		
		CurrTime.checkNew(_currTime, time);
		
		// update last item
		final List<TItem<TDate>> movedItems = iter.getMovedItems();
		if (movedItems.size() > 0) {
			_lastAvailableItem = movedItems.get(movedItems.size()-1);
		}
		
		// check if expired
		if (_lastAvailableItem != null) {
			
			final Days diff = Days.daysBetween(_lastAvailableItem.getTime().get(), time.get());
			
			if (diff.getDays() < 0) {
				throw new IllegalStateException(
						"Times are not in chronological order in \"" + getFieldName() + 
						"\"; last known time: " + _lastAvailableItem.getTime() + "; new time: " + time);
			}
			
			// set last item
			if (diff.compareTo(_aliveDays) <= 0) {
				_currItem = new TItem<TDate>(time, _lastAvailableItem.getObject());
			} else if (_defaultValue != null) {
				_currItem = new TItem<TDate>(time, _defaultValue);
			} else {
				_currItem = null;
			}
		}
		
		_movedItems.clear();
		if (_currItem != null) {
			_movedItems.add(_currItem);
		}
		
		_currTime = time;
	}

	@Override
	public TItem<TDate> getCurrItem() {
		CurrTime.checkSet(_currTime);
		return _currItem;
	}

	@Override
	public List<TItem<TDate>> getMovedItems() {
		CurrTime.checkSet(_currTime);
		return _movedItems;
	}

}
