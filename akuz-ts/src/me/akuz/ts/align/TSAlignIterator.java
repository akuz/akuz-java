package me.akuz.ts.align;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

import me.akuz.ts.TFrame;
import me.akuz.ts.TSeq;
import me.akuz.ts.TItem;

public final class TSAlignIterator<K, T extends Comparable<T>> {

	private final Map<K, TSeq<T>> _map;
	private final List<T> _times;
	private final Set<K> _keys;
	private int _timeCursor;
	private T _currTime;
	private final Map<K, Integer> _keyCursors;
	private final Map<K, TSFiller<T>> _keyFillers;
	private final Map<K, TSChecker<T>> _keyCheckers;
	private final TSAlignLog _alignLog;
	private final Map<K, TItem<T>> _currKeyItems;
	
	public TSAlignIterator(
			TFrame<K, T> inputFrame,
			final Collection<T> times,
			final Set<K> keys) {
		
		this(
			inputFrame,
			times, 
			keys,
			null,
			null,
			null);
	}
	
	public TSAlignIterator(
			TFrame<K, T> inputFrame,
			final Collection<T> times,
			final Set<K> keys,
			final TSFiller<T> filler,
			final TSChecker<T> checker,
			final TSAlignLog alignLog) {
		
		_map = inputFrame.getMap();
		if (times instanceof List<?> &&
			times instanceof RandomAccess) {
			_times = (List<T>)times;
		} else {
			_times = new ArrayList<>(times);
			if (_times.size() > 1) {
				Collections.sort(_times);
			}
		}
		_keys = keys;
		_timeCursor = -1;
		_keyCursors = new HashMap<>();
		for (K key : keys) {
			_keyCursors.put(key, 0);
		}
		if (filler != null) {
			_keyFillers = new HashMap<>();
			for (K key : keys) {
				_keyFillers.put(key, filler.clone());
			}
		} else {
			_keyFillers = null;
		}
		if (checker != null) {
			_keyCheckers = new HashMap<>();
			for (K key : keys) {
				_keyCheckers.put(key, checker.clone());
			}
		} else {
			_keyCheckers = null;
		}
		_alignLog = alignLog;
		_currKeyItems = new HashMap<>();
	}
	
	public boolean hasNext() {
		return _timeCursor + 1 < _times.size();
	}
	
	public T getCurrTime() {
		return _currTime;
	}
	
	public Map<K, TItem<T>> next() {
		
		_timeCursor++;

		_currKeyItems.clear();
		
		_currTime = _times.get(_timeCursor);

		for (final K key : _keys) {
			
			final TSeq<T> seq = _map.get(key);
			if (seq != null) {
				
				final List<TItem<T>> items = seq.getItems();
				Integer cursor = _keyCursors.get(key);
				TItem<T> currItem = null;
				
				while (true) {
					
					TItem<T> item = null;
					int cmp = 1;
					
					if (cursor < items.size()) {
						item = items.get(cursor);
						cmp = item.getTime().compareTo(_currTime);
					}

					// cursor after current time
					if (cmp > 0) {
						item = null;
					}

					if (_keyFillers != null) {
						TSFiller<T> filler = _keyFillers.get(key);
						if (filler != null) {
							TSAlignLogMsg msg = filler.next(_currTime, item);
							if (_alignLog != null && msg != null) {
								_alignLog.add(msg);
							}
							item = filler.getCurrent();
						}
					}

					if (_keyCheckers != null) {
						TSChecker<T> checker = _keyCheckers.get(key);
						if (checker != null) {
							TSAlignLogMsg msg = checker.next(_currTime, item);
							if (_alignLog != null && msg != null) {
								_alignLog.add(msg);
							}
						}
					}

					currItem = item;

					// cursor after current time
					if (cmp > 0) {
						break;
					}

					// at or before current time, move forward
					_keyCursors.put(key, ++cursor);

					// at current time
					if (cmp == 0) {
						break;
					}
				}

				if (currItem != null) {
					_currKeyItems.put(key, currItem);
				}
				
			}
		}
		
		return _currKeyItems;
	}
}
