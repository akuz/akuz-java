package me.akuz.ts;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.core.Out;
import me.akuz.ts.log.TLog;

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
implements FrameCursor<K, T> {
	
	private final FrameCursor<K, T> _frameCursor;
	private final boolean _moveCursor;
	private final Map<K, SeqFilter<T>> _seqFilters;
	private final Index<K> _keysIndex;
	private TLog<T> _log;
	private final Map<K, TItem<T>> _currItems;
	private final Map<K, List<TItem<T>>> _movedItems;
	private T _currTime;

	public FrameFilter(final FrameCursor<K, T> frameCursor) {
		this(frameCursor, true);
	}
	
	public FrameFilter(final FrameCursor<K, T> frameCursor, final boolean moveCursor) {
		if (frameCursor == null) {
			throw new IllegalArgumentException("Cannot filter null frame cursor");
		}
		_frameCursor = frameCursor;
		_moveCursor = moveCursor;
		_seqFilters = new HashMap<>();
		_keysIndex = new HashIndex<>();
		_currItems = new HashMap<>();
		_movedItems = new HashMap<>();
	}
	
	public FrameFilter<K, T> addFilters(final K key, final Collection<Filter<T>> filters) {
		for (final Filter<T> filter : filters) {
			addFilter(key, filter);
		}
		return this;
	}
	
	public FrameFilter<K, T> addFilters(final Collection<K> keys, final Collection<Filter<T>> filters) {
		for (final K key : keys) {
			for (final Filter<T> filter : filters) {
				addFilter(key, filter);
			}
		}
		return this;
	}
	
	public FrameFilter<K, T> addFilter(final Collection<K> keys, final Filter<T> filter) {
		for (final K key : keys) {
			addFilter(key, filter);
		}
		return this;
	}
	
	public FrameFilter<K, T> addFilter(final K key, final Filter<T> filter) {
		SeqFilter<T> seqFilter = _seqFilters.get(key);
		if (seqFilter == null) {
			seqFilter = new SeqFilter<>(_frameCursor.getSeqCursor(key), false);
			seqFilter.setFieldName(key.toString());
			seqFilter.setLog(_log);
			_seqFilters.put(key, seqFilter);
			_keysIndex.ensure(key);
		}
		seqFilter.addFilter(filter);
		return this;
	}
	
	public FrameFilter<K, T> setLog(final TLog<T> log) {
		_log = log;
		for (final SeqFilter<T> seqFilter : _seqFilters.values()) {
			seqFilter.setLog(log);
		}
		return this;
	}
	
	@Override
	public Frame<K, T> getFrame() {
		return _frameCursor.getFrame();
	}
	
	@Override
	public SeqCursor<T> getSeqCursor(final K key) {
		Integer index = _keysIndex.getIndex(key);
		if (index == null) {
			throw new IllegalArgumentException("Sequence for key '" + key + "' does not exist");
		}
		return _seqFilters.get(key);
	}
	
	@Override
	public List<K> getKeys() {
		return _keysIndex.getList();
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
		
		for (int i=0; i<_keysIndex.size(); i++) {
			
			final K key = _keysIndex.getValue(i);
			
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
		
		if (_keysIndex.size() == 0) {
			throw new IllegalStateException(
					"FrameFilter does not have any 1D filters assigned");
		}
		
		CurrTime.checkNew(_currTime, time);
		
		if (_moveCursor) {
			_frameCursor.moveToTime(time);
		}
		
		_currItems.clear();
		_movedItems.clear();
		for (int i=0; i<_keysIndex.size(); i++) {
			
			final K key = _keysIndex.getValue(i);
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
