package me.akuz.ts;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Time series map.
 *
 * @param <K> - Key type.
 * @param <T> - Time type.
 */
public final class TSMap<K, T extends Comparable<T>> {
	
	private final Map<K, TS<T>> _map;
	private final Map<K, TS<T>> _mapReadOnly;
	private final Set<T> _times;
	private final Set<T> _timesReadOnly;
	
	public TSMap() {
		_map = new HashMap<>();
		_mapReadOnly = Collections.unmodifiableMap(_map);
		_times = new HashSet<>();
		_timesReadOnly = Collections.unmodifiableSet(_times);
	}
	
	public void add(K key, T time, Object value) {
		add(key, new TSItem<T>(time, value));
	}
	
	public void add(K key, TSItem<T> item) {
		TS<T> ts = (TS<T>)_map.get(key);
		if (ts == null) {
			ts = new TS<>();
			_map.put(key, ts);
		}
		ts.add(item);
		_times.add(item.getTime());
	}
	
	public void add(K key, TS<T> ts) {
		if (_map.containsKey(key)) {
			throw new IllegalStateException("TS for key " + key + " has already been added");
		}
		_map.put(key, ts);
		List<TSItem<T>> tsItems = ts.getItems();
		for (int i=0; i<tsItems.size(); i++) {
			_times.add(tsItems.get(i).getTime());
		}
	}
	
	public Set<K> getKeys() {
		return _mapReadOnly.keySet();
	}
	
	public Set<T> getTimes() {
		return _timesReadOnly;
	}
	
	public Map<K, TS<T>> getMap() {
		return _mapReadOnly;
	}
}
