package me.akuz.ts;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Builds time series map from unsorted data.
 *
 * @param <K> - Keys type.
 * @param <T> - Time type.
 */
public class TSBuildMap<K, T extends Comparable<T>> {
	
	private final Map<K, TSBuild<T>> _map;
	
	public TSBuildMap() {
		_map = new HashMap<>();
	}
	
	public void add(K key, T time, Object value) {
		add(key, new TSItem<T>(time, value));
	}
	
	public void add(K key, TSItem<T> entry) {
		TSBuild<T> tsBuilder =_map.get(key);
		if (tsBuilder == null) {
			tsBuilder = new TSBuild<>();
			_map.put(key, tsBuilder);
		}
		tsBuilder.add(entry);
	}
	
	public TSMap<K, T> build() {
		TSMap<K, T> tsMap = new TSMap<>();
		for (Entry<K, TSBuild<T>> entry : _map.entrySet()) {
			
			K key = entry.getKey();
			TSBuild<T> tsBuilder = entry.getValue();
			
			TS<T> ts = tsBuilder.build();
			tsMap.add(key, ts);
		}
		return tsMap;
	}
}
