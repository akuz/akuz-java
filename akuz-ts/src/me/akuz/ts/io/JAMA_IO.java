package me.akuz.ts.io;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.ts.Frame;
import me.akuz.ts.TItem;
import me.akuz.ts.filters.Filter;
import me.akuz.ts.filters.FrameFilter;
import me.akuz.ts.filters.FrameWalker;
import me.akuz.ts.log.TLog;
import Jama.Matrix;

/**
 * Output frames to Jama matrices.
 *
 */
public final class JAMA_IO {
	
	public static <K, T extends Comparable<T>> Matrix intoMatrix(
			final Frame<K, T> frame) {
		
		Index<K> keysIndex = new HashIndex<>();
		keysIndex.ensureAll(frame.getKeys());
		
		return intoMatrix(
				frame,
				keysIndex);
	}
	
	public static <K, T extends Comparable<T>> Matrix intoMatrix(
			final Frame<K, T> frame,
			final Index<K> keysIndex) {
		
		return intoMatrix(
				frame,
				keysIndex,
				frame.extractTimes());
	}

	public static <K, T extends Comparable<T>> Matrix intoMatrix(
			final Frame<K, T> frame,
			final Collection<T> times) {
		
		Index<K> keysIndex = new HashIndex<>();
		keysIndex.ensureAll(frame.getKeys());
		
		return intoMatrix(
				frame,
				keysIndex,
				times);
	}

	public static <K, T extends Comparable<T>> Matrix intoMatrix(
			final Frame<K, T> frame,
			final Index<K> keysIndex,
			final Collection<T> times) {
		
		return intoMatrix(
				frame, 
				keysIndex,
				times, 
				null,
				null);
	}

	public static <K, T extends Comparable<T>> Matrix intoMatrix(
			final Frame<K, T> frame,
			final Index<K> keysIndex,
			final Collection<T> times,
			final List<Filter<T>> filters,
			final TLog log) {
		
		final Matrix m = new Matrix(times.size(), keysIndex.size(), Double.NaN);
		int i = 0;

		final FrameWalker<K, T> frameAligner = new FrameWalker<>(frame, keysIndex.getMap().keySet(), times);
		
		final FrameFilter.Builder<K, T> frameFilterBuilder = FrameFilter.onAllKeysOf(frameAligner);
		if (filters != null) {
			for (final Filter<T> filter : filters) {
				frameFilterBuilder.addAllKeysFilter(filter);
			}
		}
		frameFilterBuilder.setLog(log);
		
		final FrameFilter<K, T> frameFilter = frameFilterBuilder.build();
		
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

//	public <K> TFrame<K, T> intoFrame(Set<K> keys, TFrame<K, T> frame) {
//		return intoFrame(keys, frame, null, null);
//	}
//
//	public <K> TFrame<K, T> intoFrame(Set<K> keys, TFrame<K, T> frame, List<TFilter<T>> filters, TLog log) {
//		
//		TFrame<K, T> result = new TFrame<>();
//
//		final TFrameWalker<K, T> frameAligner = new TFrameWalker<>(frame, keys, _times);
//		
//		final TFrameFilter.Builder<K, T> frameFilterBuilder = TFrameFilter.onAllKeysOf(frameAligner);
//		for (TFilter<T> filter : filters) {
//			frameFilterBuilder.addAllKeysFilter(filter);
//		}
//		frameFilterBuilder.setLog(log);
//		
//		final TFrameFilter<K, T> frameFilter = frameFilterBuilder.build();
//		
//		while (frameFilter.hasNext()) {
//			
//			frameFilter.next();
//			
//			final Map<K, TItem<T>> currKeyValues = frameFilter.getCurrItems();
//
//			for (final K key : keys) {
//	
//				final TItem<T> item = currKeyValues.get(key);
//				
//				if (item != null) {
//					result.add(key, item);
//				}
//			}
//		}
//
//		return result;
//	}

}
