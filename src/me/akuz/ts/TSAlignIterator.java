package me.akuz.ts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TSAlignIterator<K, T extends Comparable<T>> {

	private final Map<K, TS<T>> _map;
	private final List<T> _times;
	private final Set<K> _keys;
	private int _timeCursor;
	private T _currTime;
	private final Map<K, Integer> _keyCursors;
	private final Map<K, TSEntry<T>> _currKeyEntries;
	
	public TSAlignIterator(
			Map<K, TS<T>> map,
			final List<T> times,
			final Set<K> keys) {
		
		_map = map;
		_times = times;
		_keys = keys;
		_timeCursor = -1;
		_keyCursors = new HashMap<>();
		for (K key : keys) {
			_keyCursors.put(key, 0);
		}
		_currKeyEntries = new HashMap<>();
	}
	
	
	public boolean hasNext() {
		return _timeCursor + 1 < _times.size();
	}
	
	public T getCurrTime() {
		return _currTime;
	}
	
	public Map<K, TSEntry<T>> next() {
		
		_timeCursor++;

		_currKeyEntries.clear();
		
		_currTime = _times.get(_timeCursor);

		for (final K key : _keys) {
			
			final TS<T> ts = _map.get(key);
			if (ts != null) {
				
				final List<TSEntry<T>> tsSorted = ts.getSorted();
				
				Integer cursor = _keyCursors.get(key);
				
				TSEntry<T> currEntry = null;
				
				while (cursor < tsSorted.size()) {
					
					final TSEntry<T> entry = tsSorted.get(cursor);

					final int cmp = entry.getTime().compareTo(_currTime);

					if (cmp > 0) {
						// not reached current time, don't move
						break;
					}
					if (cmp < 0) {
						// before current time, move forward
						_keyCursors.put(key, ++cursor);
						continue;
					}
					
					// at current time, set matrix value
					_keyCursors.put(key, ++cursor);
					currEntry = entry;
				}

				if (currEntry != null) {
					_currKeyEntries.put(key, currEntry);
				}
			}
		}
		
		return _currKeyEntries;
	}
}
