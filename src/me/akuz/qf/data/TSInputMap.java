package me.akuz.qf.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Input time series map.
 *
 * @param <K> - Keys type.
 * @param <T> - Time type.
 */
public class TSInputMap<K, T extends Comparable<T>> {
	
	private final Map<K, TSInput<T>> _map;
	private final Map<K, TSInput<T>> _mapReadOnly;
	
	public TSInputMap() {
		_map = new HashMap<>();
		_mapReadOnly = Collections.unmodifiableMap(_map);
	}
	
	public void add(K key, T time, Object value) {
		add(key, new TSEntry<T>(time, value));
	}
	
	public void add(K key, TSEntry<T> entry) {
		TSInput<T> ts = _map.get(key);
		if (ts == null) {
			ts = new TSInput<>();
			_map.put(key, ts);
		}
		ts.add(entry);
	}
	
	public Map<K, TSInput<T>> getMap() {
		return _mapReadOnly;
	}
	
	public void sortAll() {
		for (TSInput<T> ts : _map.values()) {
			ts.sort();
		}
	}
}
