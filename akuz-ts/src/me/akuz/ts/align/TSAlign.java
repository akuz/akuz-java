package me.akuz.ts.align;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Index;
import me.akuz.ts.TS;
import me.akuz.ts.TSItem;
import me.akuz.ts.TSMap;
import Jama.Matrix;

/**
 * Align operator that allows extracting time series values matching the specified times schedule.
 *
 */
public final class TSAlign<T extends Comparable<T>> {
	
	private final List<T> _times;
	
	public TSAlign(Set<T> times) {
		_times = new ArrayList<>(times);
		if (_times.size() > 1) {
			Collections.sort(_times);
		}
	}
	
	public List<T> getTimes() {
		return _times;
	}

	public <K> Matrix intoMatrix(Index<K> keysIndex, Map<K, TS<T>> map) {
		return intoMatrix(keysIndex, map, null, null, null);
	}

	public <K> Matrix intoMatrix(Index<K> keysIndex, Map<K, TS<T>> map, TSFiller<T> filler, TSChecker<T> checker, TSAlignLog alignLog) {
		
		if (keysIndex == null) {
			throw new IllegalArgumentException("Argument colsIndex must not be null");
		}
		
		final Matrix m = new Matrix(_times.size(), keysIndex.size(), Double.NaN);
		int i = 0;

		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(map, _times, keysIndex.getMap().keySet(), filler, checker, alignLog);
		while (iterator.hasNext()) {
			
			final Map<K, TSItem<T>> currKeyValues = iterator.next();

			for (int j=0; j<keysIndex.size(); j++) {
				
				K key = keysIndex.getValue(j);
				TSItem<T> item = currKeyValues.get(key);
				if (item != null) {
					m.set(i, j, item.getNumber().doubleValue());
				} else {
					m.set(i, j, Double.NaN);
				}
			}
			i++;
		}

		return m;
	}

	public <K> TSMap<K, T> intoTSMap(Set<K> keys, Map<K, TS<T>> map) {
		return intoTSMap(keys, map, null, null, null);
	}

	public <K> TSMap<K, T> intoTSMap(Set<K> keys, Map<K, TS<T>> map, TSFiller<T> filler, TSChecker<T> checker, TSAlignLog alignLog) {
		
		TSMap<K, T> tsMap = new TSMap<>();

		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(map, _times, keys, filler, checker, alignLog);
		while (iterator.hasNext()) {
			
			final Map<K, TSItem<T>> currKeyValues = iterator.next();

			for (K key : keys) {
	
				TSItem<T> item = currKeyValues.get(key);
				
				if (item != null) {
					tsMap.add(key, item);
				}
			}
		}

		return tsMap;
	}

}
