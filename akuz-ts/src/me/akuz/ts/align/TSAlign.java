package me.akuz.ts.align;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Index;
import me.akuz.ts.TItem;
import me.akuz.ts.TFrame;
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

	public <K> Matrix intoMatrix(Index<K> keysIndex, TFrame<K, T> inputFrame) {
		return intoMatrix(keysIndex, inputFrame, null, null, null);
	}

	public <K> Matrix intoMatrix(Index<K> keysIndex, TFrame<K, T> inputFrame, TSFiller<T> filler, TSChecker<T> checker, TSAlignLog alignLog) {
		
		if (keysIndex == null) {
			throw new IllegalArgumentException("Argument colsIndex must not be null");
		}
		
		final Matrix m = new Matrix(_times.size(), keysIndex.size(), Double.NaN);
		int i = 0;

		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(inputFrame, _times, keysIndex.getMap().keySet(), filler, checker, alignLog);
		while (iterator.hasNext()) {
			
			final Map<K, TItem<T>> currKeyValues = iterator.next();

			for (int j=0; j<keysIndex.size(); j++) {
				
				K key = keysIndex.getValue(j);
				TItem<T> item = currKeyValues.get(key);
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

	public <K> TFrame<K, T> intoFrame(Set<K> keys, TFrame<K, T> inputFrame) {
		return intoFrame(keys, inputFrame, null, null, null);
	}

	public <K> TFrame<K, T> intoFrame(Set<K> keys, TFrame<K, T> inputFrame, TSFiller<T> filler, TSChecker<T> checker, TSAlignLog alignLog) {
		
		TFrame<K, T> frame = new TFrame<>();

		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(inputFrame, _times, keys, filler, checker, alignLog);
		while (iterator.hasNext()) {
			
			final Map<K, TItem<T>> currKeyValues = iterator.next();

			for (K key : keys) {
	
				TItem<T> item = currKeyValues.get(key);
				
				if (item != null) {
					frame.add(key, item);
				}
			}
		}

		return frame;
	}

}
