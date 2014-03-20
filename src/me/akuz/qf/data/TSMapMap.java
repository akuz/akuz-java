package me.akuz.qf.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Map of time series maps.
 *
 * @param <K1> - Level 1 key type.
 * @param <K2> - Level 2 key type.
 * @param <T> - Time type.
 */
public final class TSMapMap<K1, K2, T extends Comparable<T>> {

	private final Set<T> _times;
	private final Set<T> _timesReadOnly;
	private final Map<K1, Map<K2, TS<T>>> _mapMap;
	private final Map<K1, Map<K2, TS<T>>> _mapMapReadOnly;
	
	public TSMapMap() {
		_times = new HashSet<>();
		_timesReadOnly = Collections.unmodifiableSet(_times);
		_mapMap = new HashMap<>();
		_mapMapReadOnly = Collections.unmodifiableMap(_mapMap);
	}
	
	public void add(K1 key1, TSMap<K2, T> map) {
		if (_mapMap.containsKey(key1)) {
			throw new IllegalStateException("Map for key1 " + key1 + " has already been added before");
		}
		_mapMap.put(key1, map.getMap());
		_times.addAll(map.getTimes());
	}
	
	public Set<T> getTimes() {
		return _timesReadOnly;
	}
	
	public Map<K1, Map<K2, TS<T>>> getMapMap() {
		return _mapMapReadOnly;
	}
	
	public Map<K2, Map<K1, TS<T>>> reshuffle() {
		
		final Map<K2, Map<K1, TS<T>>> resultMapMap = new HashMap<>();
		for (Entry<K1, Map<K2, TS<T>>> entry1 : _mapMap.entrySet()) {
			
			final K1 key1 = entry1.getKey();
			final Map<K2, TS<T>> map = entry1.getValue();
			
			for (Entry<K2, TS<T>> entry2 : map.entrySet()) {
				
				final K2 key2 = entry2.getKey();
				final TS<T> ts = entry2.getValue();
				
				Map<K1, TS<T>> resultMap = resultMapMap.get(key2);
				if (resultMap == null) {
					resultMap = new HashMap<>();
					resultMapMap.put(key2, resultMap);
				}
				
				resultMap.put(key1, ts);
			}
		}
		return resultMapMap;
	}
	
}
