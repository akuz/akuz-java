package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.Out;
import me.akuz.ts.log.TLog;
import me.akuz.ts.sync.Synchronizable;

/**
 * {@link Frame} filter, which applies one or more 
 * 1D filters to the selected sequences within the 
 * underlying frame, and allows to obtain current 
 * filtered items for each sequence.
 * 
 * Only one of the filters is allowed to generate
 * the current item for each sequence, otherwise 
 * an exception will occur.
 *
 */
public final class FrameFilter<K, T extends Comparable<T>> 
implements Synchronizable<T>, FrameCursor<K, T> {
	
	private final Frame<K, T> _frame;
	private final Map<K, SeqFilter<T>> _seqFilters;
	private final List<K> _filterKeys;
	private TLog<T> _log;
	private final Map<K, TItem<T>> _currItems;
	private final Map<K, List<TItem<T>>> _movedItems;
	private T _currTime;
	
	public FrameFilter(final Frame<K, T> frame) {
		if (frame == null) {
			throw new IllegalArgumentException("Cannot filter null frame");
		}
		_frame = frame;
		_seqFilters = new HashMap<>();
		_filterKeys = new ArrayList<>();
		_currItems = new HashMap<>();
		_movedItems = new HashMap<>();
	}
	
	public void addFilters(final K key, Collection<Filter<T>> filters) {
		for (final Filter<T> filter : filters) {
			addFilter(key, filter);
		}
	}
	
	public void addFilters(final Collection<K> keys, Collection<Filter<T>> filters) {
		for (final K key : keys) {
			for (final Filter<T> filter : filters) {
				addFilter(key, filter);
			}
		}
	}
	
	public void addFilter(final Collection<K> keys, Filter<T> filter) {
		for (final K key : keys) {
			addFilter(key, filter);
		}
	}
	
	public void addFilter(final K key, Filter<T> filter) {
		SeqFilter<T> seqFilter = _seqFilters.get(key);
		if (seqFilter == null) {
			seqFilter = new SeqFilter<>(_frame.getSeq(key).iterator());
			seqFilter.setFieldName(key.toString());
			seqFilter.setLog(_log);
			_seqFilters.put(key, seqFilter);
			_filterKeys.add(key);
		}
		seqFilter.addFilter(filter);
	}
	
	public void setLog(final TLog<T> log) {
		_log = log;
		for (final SeqFilter<T> seqFilter : _seqFilters.values()) {
			seqFilter.setLog(log);
		}
	}
	
	@Override
	public List<K> getKeys() {
		return _filterKeys;
	}
	
	@Override
	public T getCurrTime() {
		CurrTime.checkSet(_currTime);
		return _currTime;
	}
	
	@Override
	public TItem<T> getCurrItem(final K key) {
		CurrTime.checkSet(_currTime);
		return _currItems.get(key);
	}
	
	@Override
	public Map<K, TItem<T>> getCurrItems() {
		CurrTime.checkSet(_currTime);
		return _currItems;
	}
	
	@Override
	public List<TItem<T>> getMovedItems(final K key) {
		CurrTime.checkSet(_currTime);
		return _movedItems.get(key);
	}
	
	@Override
	public Map<K, List<TItem<T>>> getMovedItems() {
		CurrTime.checkSet(_currTime);
		return _movedItems;
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {
		
		nextTime.setValue(null);
		
		final Out<T> seqNextTime = new Out<>();
		
		for (int i=0; i<_filterKeys.size(); i++) {
			
			final K key = _filterKeys.get(i);
			
			final SeqFilter<T> seqFilter = _seqFilters.get(key);
			
			if (seqFilter.getNextTime(seqNextTime)) {
				
				if (nextTime.getValue() == null ||
					nextTime.getValue().compareTo(seqNextTime.getValue()) > 0) {
					
					nextTime.setValue(seqNextTime.getValue());
				}
			}
		}
		
		return nextTime.getValue() != null;
	}
	
	@Override
	public void moveToTime(final T time) {
		
		if (_filterKeys.size() == 0) {
			throw new IllegalStateException(
					"FrameFilter does not have any 1D filters assigned");
		}
		
		CurrTime.checkNew(_currTime, time);
		
		_currItems.clear();
		_movedItems.clear();
		for (int i=0; i<_filterKeys.size(); i++) {
			
			final K key = _filterKeys.get(i);
			final SeqFilter<T> seqFilter = _seqFilters.get(key);
			seqFilter.moveToTime(time);
			if (seqFilter.getCurrItem() != null) {
				_currItems.put(key, seqFilter.getCurrItem());
			}
			if (seqFilter.getMovedItems() != null &&
				seqFilter.getMovedItems().size() > 0) {
				_movedItems.put(key, seqFilter.getMovedItems());
			}
		}
		_currTime = time;
	}

}
