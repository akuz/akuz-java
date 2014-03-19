package me.akuz.qf.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Time series map.
 *
 * @param <K> - Keys type.
 * @param <T> - Time type.
 */
public class TSMap<K, T extends Comparable<T>> {
	
	private final Map<K, TS<T>> _map;
	private final Map<K, TS<T>> _mapReadOnly;
	
	public TSMap() {
		_map = new HashMap<>();
		_mapReadOnly = Collections.unmodifiableMap(_map);
	}
	
	public void add(K key, T time, Object value) {
		add(key, new TSEntry<T>(time, value));
	}
	
	public void add(K key, TSEntry<T> entry) {
		TS<T> ts = _map.get(key);
		if (ts == null) {
			ts = new TS<>();
			_map.put(key, ts);
		}
		ts.add(entry);
	}
	
	public Map<K, TS<T>> getMap() {
		return _mapReadOnly;
	}
}
