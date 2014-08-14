package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.Out;
import me.akuz.ts.Frame;
import me.akuz.ts.FrameCursor;
import me.akuz.ts.TItem;
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
	private TLog _log;
	private final Map<K, TItem<T>> _currItems;
	private T _currTime;
	
	public FrameFilter(final Frame<K, T> frame) {
		if (frame == null) {
			throw new IllegalArgumentException("Cannot filter null frame");
		}
		_frame = frame;
		_seqFilters = new HashMap<>();
		_filterKeys = new ArrayList<>();
		_currItems = new HashMap<>();
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
			seqFilter = new SeqFilter<>(_frame.getSeq(key));
			seqFilter.setFieldName(key.toString());
			seqFilter.setLog(_log);
			_seqFilters.put(key, seqFilter);
			_filterKeys.add(key);
		}
		seqFilter.addFilter(filter);
	}
	
	public void setLog(final TLog log) {
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
		return _currTime;
	}
	
	@Override
	public Map<K, TItem<T>> getCurrItems() {
		return _currItems;
	}
	
	@Override
	public TItem<T> getCurrItem(final K key) {
		return _currItems.get(key);
	}
	
	@Override
	public boolean getNextTime(Out<T> nextTime) {
		
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
	public void moveToTime(T time) {
		
		if (_filterKeys.size() == 0) {
			throw new IllegalStateException(
					"FrameFilter does not have any 1D filters assigned");
		}
		
		if (_currTime != null) {
			final int cmp = _currTime.compareTo(time);
			if (cmp > 0)
				throw new IllegalStateException(
						"Trying to move backwards in time from " + 
						_currTime + " to " + time);
			if (cmp == 0)
				return;
		}
		
		_currItems.clear();
		for (int i=0; i<_filterKeys.size(); i++) {
			
			final K key = _filterKeys.get(i);
			final SeqFilter<T> seqFilter = _seqFilters.get(key);
			seqFilter.moveToTime(time);
			_currItems.put(key, seqFilter.getCurrItem());
		}
		_currTime = time;
	}

}
