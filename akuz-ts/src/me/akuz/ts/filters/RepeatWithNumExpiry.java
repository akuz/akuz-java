package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class RepeatWithNumExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private final Number _alivePeriod;
	private final Object _defaultValue;
	private TItem<T> _lastAvailableItem;
	private final List<TItem<T>> _movedItems;
	private TItem<T> _currItem;
	private T _currTime;
	
	public RepeatWithNumExpiry(final int aliveCount) {
		this(aliveCount, null);
	}
	
	public RepeatWithNumExpiry(final Number alivePeriod, Object defaultValue) {
		_alivePeriod = alivePeriod;
		_defaultValue = defaultValue;
		_movedItems = new ArrayList<>();
	}

	@Override
	public void next(
			final TLog<T> log,
			final T time,
			final SeqCursor<T> iter) {
		
		CurrTime.checkNew(_currTime, time);

		// update last item
		final List<TItem<T>> movedItems = iter.getMovedItems();
		if (movedItems.size() > 0) {
			_lastAvailableItem = movedItems.get(movedItems.size()-1);
		}
		
		// check if expired
		if (_lastAvailableItem != null) {
			
			final double diff 
							= ((Number)time).doubleValue() 
							- ((Number)_lastAvailableItem.getTime()).doubleValue();
			
			if (diff < 0) {
				throw new IllegalStateException(
						"Times are not in chronological order in \"" + getFieldName() + 
						"\"; last known time: " + _lastAvailableItem + "; new time: " + time);
			}
			
			// set last item
			if (diff <= _alivePeriod.doubleValue()) {
				_currItem = new TItem<T>(time, _lastAvailableItem.getObject());
			} else if (_defaultValue != null) {
				_currItem = new TItem<T>(time, _defaultValue);
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
