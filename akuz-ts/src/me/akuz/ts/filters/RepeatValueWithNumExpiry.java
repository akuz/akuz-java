package me.akuz.ts.filters;

import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class RepeatValueWithNumExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private final Number _alivePeriod;
	private final Object _defaultValue;
	private TItem<T> _lastAvailableItem;
	
	public RepeatValueWithNumExpiry(final int aliveCount) {
		this(aliveCount, null);
	}
	
	public RepeatValueWithNumExpiry(final Number alivePeriod, Object defaultValue) {
		_alivePeriod = alivePeriod;
		_defaultValue = defaultValue;
	}

	@Override
	public void next(
			final T time,
			final SeqCursor<T> cur,
			final TLog<T> log) {
		
		CurrTime.checkNew(_currTime, time);

		// update last item
		final List<TItem<T>> movedItems = cur.getMovedItems();
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

}
