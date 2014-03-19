package me.akuz.qf.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Input time series map.
 *
 * @param <K> - Keys type.
 * @param <T> - Time type.
 */
public class TSInputMap<K, T extends Comparable<T>> extends TSMap<K, T> {
	
	private final Map<K, TS<T>> _map;
	private final Map<K, TS<T>> _mapReadOnly;
	private final Set<T> _times;
	private final Set<T> _timesReadOnly;
	
	public TSInputMap() {
		_map = new HashMap<>();
		_mapReadOnly = Collections.unmodifiableMap(_map);
		_times = new HashSet<>();
		_timesReadOnly = Collections.unmodifiableSet(_times);
	}
	
	public void add(K key, T time, Object value) {
		add(key, new TSEntry<T>(time, value));
	}
	
	public void add(K key, TSEntry<T> entry) {
		TSInput<T> ts = (TSInput<T>)_map.get(key);
		if (ts == null) {
			ts = new TSInput<>();
			_map.put(key, ts);
		}
		_times.add(entry.getTime());
		ts.add(entry);
	}
	
	public void sortAll() {
		for (TS<T> ts : _map.values()) {
			((TSInput<T>)ts).sort();
		}
	}
	
	@Override
	public Set<K> getKeys() {
		return _mapReadOnly.keySet();
	}
	
	@Override
	public Set<T> getTimes() {
		return _timesReadOnly;
	}
	
	@Override
	public Map<K, TS<T>> getMap() {
		return _mapReadOnly;
	}
}
