package me.akuz.ts;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Output time series map.
 *
 * @param <K> - Keys type.
 * @param <T> - Time type.
 */
public class TSOutputMap<K, T extends Comparable<T>> extends TSMap<K, T> {

	private final Map<K, TS<T>> _map;
	private final Map<K, TS<T>> _mapReadOnly;
	private final Set<T> _times;
	private final Set<T> _timesReadOnly;
	
	public TSOutputMap() {
		_map = new HashMap<>();
		_mapReadOnly = Collections.unmodifiableMap(_map);
		_times = new HashSet<>();
		_timesReadOnly = Collections.unmodifiableSet(_times);
	}
	
	public void add(K key, T time, Object value) {
		add(key, new TSEntry<T>(time, value));
	}
	
	public void add(K key, TSEntry<T> entry) {
		TSOutput<T> ts = (TSOutput<T>)_map.get(key);
		if (ts == null) {
			ts = new TSOutput<>();
			_map.put(key, ts);
		}
		_times.add(entry.getTime());
		ts.add(entry);
	}
	
	public void add(K key, TS<T> ts) {
		if (_map.containsKey(key)) {
			throw new IllegalStateException("TS for key " + key + " has already been added");
		}
		_map.put(key, ts);
		List<TSEntry<T>> tsSorted = ts.getSorted();
		for (int i=0; i<tsSorted.size(); i++) {
			_times.add(tsSorted.get(i).getTime());
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
