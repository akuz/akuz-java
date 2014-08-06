package me.akuz.ts.align;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Index;
import me.akuz.ts.TItem;
import me.akuz.ts.TFrame;
import me.akuz.ts.filters.TFilter;
import me.akuz.ts.filters.TFrameAligner;
import me.akuz.ts.filters.TFrameFilter;
import me.akuz.ts.log.TLog;
import Jama.Matrix;

/**
 * Align operator that allows extracting time series values matching the specified times schedule.
 *
 */
public final class ToMatrix<T extends Comparable<T>> {
	
	private final List<T> _times;
	
	public ToMatrix(Set<T> times) {
		_times = new ArrayList<>(times);
		if (_times.size() > 1) {
			Collections.sort(_times);
		}
	}
	
	public List<T> getTimes() {
		return _times;
	}

	public <K> Matrix intoMatrix(
			final Index<K> keysIndex,
			final TFrame<K, T> frame) {
		
		return intoMatrix(keysIndex, frame, null, null);
	}

	public <K> Matrix intoMatrix(
			final Index<K> keysIndex,
			final TFrame<K, T> frame,
			final List<TFilter<T>> filters,
			final TLog log) {
		
		if (keysIndex == null) {
			throw new IllegalArgumentException("Argument colsIndex must not be null");
		}
		
		final Matrix m = new Matrix(_times.size(), keysIndex.size(), Double.NaN);
		int i = 0;

		final TFrameAligner<K, T> frameAligner = new TFrameAligner<>(frame, keysIndex.getMap().keySet(), _times);
		
		final TFrameFilter.Builder<K, T> frameFilterBuilder = TFrameFilter.on(frameAligner);
		if (filters != null) {
			for (final TFilter<T> filter : filters) {
				frameFilterBuilder.addAllKeysFilter(filter);
			}
		}
		frameFilterBuilder.setLog(log);
		
		final TFrameFilter<K, T> frameFilter = frameFilterBuilder.build();
		
		while (frameFilter.hasNext()) {
			
			frameFilter.next();
			
			final Map<K, TItem<T>> currKeyValues = frameFilter.getCurrItems();

			for (int j=0; j<keysIndex.size(); j++) {
				
				final K key = keysIndex.getValue(j);
				final TItem<T> item = currKeyValues.get(key);
				
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

	public <K> TFrame<K, T> intoFrame(Set<K> keys, TFrame<K, T> frame) {
		return intoFrame(keys, frame, null, null);
	}

	public <K> TFrame<K, T> intoFrame(Set<K> keys, TFrame<K, T> frame, List<TFilter<T>> filters, TLog log) {
		
		TFrame<K, T> result = new TFrame<>();

		final TFrameAligner<K, T> frameAligner = new TFrameAligner<>(frame, keys, _times);
		
		final TFrameFilter.Builder<K, T> frameFilterBuilder = TFrameFilter.on(frameAligner);
		for (TFilter<T> filter : filters) {
			frameFilterBuilder.addAllKeysFilter(filter);
		}
		frameFilterBuilder.setLog(log);
		
		final TFrameFilter<K, T> frameFilter = frameFilterBuilder.build();
		
		while (frameFilter.hasNext()) {
			
			frameFilter.next();
			
			final Map<K, TItem<T>> currKeyValues = frameFilter.getCurrItems();

			for (final K key : keys) {
	
				final TItem<T> item = currKeyValues.get(key);
				
				if (item != null) {
					result.add(key, item);
				}
			}
		}

		return result;
	}

}
