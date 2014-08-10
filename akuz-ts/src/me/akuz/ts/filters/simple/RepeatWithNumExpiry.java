package me.akuz.ts.filters.simple;

import java.util.List;

import me.akuz.ts.TItem;
import me.akuz.ts.filters.Filter;
import me.akuz.ts.log.TLog;

public final class RepeatWithNumExpiry<T extends Comparable<T>> extends Filter<T> {
	
	private String _fieldName;
	private final Number _alivePeriod;
	private final Object _defaultValue;
	private TItem<T> _lastAvailableItem;
	private TItem<T> _currFilterItem;
	
	public RepeatWithNumExpiry(final int aliveCount) {
		this(aliveCount, null);
	}
	
	public RepeatWithNumExpiry(final Number alivePeriod, Object defaultValue) {
		_alivePeriod = alivePeriod;
		_defaultValue = defaultValue;
	}

	@Override
	public void next(
			final TLog log,
			final T currTime,
			final TItem<T> currItem,
			final List<TItem<T>> movedItems) {

		// update last item
		if (movedItems.size() > 0) {
			_lastAvailableItem = movedItems.get(movedItems.size()-1);
		}
		
		// check if expired
		if (_lastAvailableItem != null) {
			
			final double diff 
							= ((Number)currTime).doubleValue() 
							- ((Number)_lastAvailableItem.getTime()).doubleValue();
			
			if (diff < 0) {
				throw new IllegalStateException(
						"Times are not in chronological order in \"" + _fieldName + 
						"\"; last known time: " + _lastAvailableItem + "; new time: " + currTime);
			}
			
			// set last item
			if (diff <= _alivePeriod.doubleValue()) {
				_currFilterItem = new TItem<T>(currTime, _lastAvailableItem.getObject());
			} else if (_defaultValue != null) {
				_currFilterItem = new TItem<T>(currTime, _defaultValue);
			} else {
				_currFilterItem = null;
			}
		}
	}

	@Override
	public TItem<T> getCurrItem() {
		return _currFilterItem;
	}

	@Override
	public void setFieldName(final String fieldName) {
		_fieldName = fieldName;
	}
	
	public String getFieldName() {
		return _fieldName != null ? _fieldName : "unspecified";
	}

}
