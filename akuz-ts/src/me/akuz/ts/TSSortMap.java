package me.akuz.ts;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Builds map of time series from unsorted items.
 *
 * @param <K> - Keys type.
 * @param <T> - Time type.
 */
public final class TSSortMap<K, T extends Comparable<T>> {
	
	private final Map<K, TSSort<T>> _map;
	
	public TSSortMap() {
		_map = new HashMap<>();
	}
	
	public void add(K key, T time, Object value) {
		add(key, new TSItem<T>(time, value));
	}
	
	public void add(K key, TSItem<T> entry) {
		TSSort<T> tsSort =_map.get(key);
		if (tsSort == null) {
			tsSort = new TSSort<>();
			_map.put(key, tsSort);
		}
		tsSort.add(entry);
	}
	
	public TSMap<K, T> build() {
		TSMap<K, T> tsMap = new TSMap<>();
		for (Entry<K, TSSort<T>> entry : _map.entrySet()) {
			
			K key = entry.getKey();
			TSSort<T> tsSort = entry.getValue();
			TS<T> ts = tsSort.build();
			tsMap.add(key, ts);
		}
		return tsMap;
	}
}
