package me.akuz.ts.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.core.Out;
import me.akuz.ts.Frame;
import me.akuz.ts.FrameCursor;
import me.akuz.ts.FrameIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.filters.Filter;
import me.akuz.ts.filters.FrameFilter;
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
				null,
				null);
	}

	public static <K, T extends Comparable<T>> Matrix intoMatrix(
			final Frame<K, T> frame,
			final Index<K> keysIndex,
			final List<Filter<T>> filters,
			final TLog log) {
		
		final List<double[]> rows = new ArrayList<>();
		
		final FrameCursor<K, T> frameCursor;
		if (filters != null) {
			
			final FrameFilter<K, T> frameFilter = new FrameFilter<>(frame);
			frameFilter.addFilters(keysIndex.getMap().keySet(), filters);
			frameFilter.setLog(log);
			frameCursor = frameFilter;
			
		} else {
			frameCursor = new FrameIterator<>(frame, keysIndex.getMap().keySet());
		}
		
		Out<T> nextTime = new Out<>();
		while (frameCursor.getNextTime(nextTime)) {
			
			frameCursor.moveToTime(nextTime.getValue());
			
			final Map<K, TItem<T>> currKeyValues = frameCursor.getCurrItems();

			final double[] row = new double[keysIndex.size()];
			
			for (int j=0; j<keysIndex.size(); j++) {
				
				final K key = keysIndex.getValue(j);
				final TItem<T> item = currKeyValues.get(key);
				
				if (item != null) {
					row[j] = item.getNumber().doubleValue();
				} else {
					row[j] = Double.NaN;
				}
			}
			rows.add(row);
		}
		
		final double[][] arr = new double[rows.size()][keysIndex.size()];
		for (int i=0; i<rows.size(); i++) {
			arr[i] = rows.get(i);
		}

		return new Matrix(arr);
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
