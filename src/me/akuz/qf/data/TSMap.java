package me.akuz.qf.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Index;
import Jama.Matrix;

/**
 * Abstract time series map.
 *
 * @param <K> - Key type.
 * @param <T> - Time type.
 */
public abstract class TSMap<K, T extends Comparable<T>> {
	
	public abstract Set<K> getKeys();
	
	public abstract Set<T> getTimes();

	public abstract Map<K, TS<T>> getMap();
	
	public Matrix alignIntoMatrix(Index<K> keysIndex) {
		return alignIntoMatrix(keysIndex, null);
	}
	
	public Matrix alignIntoMatrix(Index<K> keysIndex, List<T> outRowTimes) {
		
		if (keysIndex == null) {
			throw new IllegalArgumentException("Argument keysIndex must not be null");
		}
		
		final Set<T> times = getTimes();
		if (outRowTimes != null) {
			if (outRowTimes.size() > 0) {
				throw new IllegalArgumentException("Argument outRowTimes must be an an empty list, or null");
			} else {
				outRowTimes.addAll(times);
				if (outRowTimes.size() > 1) {
					Collections.sort(outRowTimes);
				}
			}
		}
		
		final Map<K, TS<T>> map = getMap();
		final int[] jCursors = new int[keysIndex.size()];
		final Matrix m = new Matrix(times.size(), keysIndex.size());
		
		for (int i=0; i<m.getRowDimension(); i++) {
			
			// find curr time
			T currTime = null;
			for (int j=0; j<jCursors.length; j++) {
				
				final int jCursor = jCursors[j];
				final TS<T> jTS = map.get(keysIndex.getValue(j));
				final List<TSEntry<T>> jSorted = jTS.getSorted();
				
				if (jTS != null && jCursor < jSorted.size()) {
					T time = jSorted.get(jCursor).getTime();
					if (currTime == null || 
						currTime.compareTo(time) > 0) {
						currTime = time;
					}
				}
			}
			
			// check curr time
			if (currTime == null) {
				throw new InternalError("Curr time for matrix row " + i + " not found");
			}
			
			// populate row
			for (int j=0; j<jCursors.length; j++) {
				
				final int jCursor = jCursors[j];
				final TS<T> jTS = map.get(keysIndex.getValue(j));
				final List<TSEntry<T>> jSorted = jTS.getSorted();
				
				if (jTS != null && jCursor < jSorted.size()) {
					TSEntry<T> entry = jSorted.get(jCursor);
					if (entry.getTime().equals(currTime)) {
						final double value = entry.getDouble();
						m.set(i, j, value);
						jCursors[j]++;
					}
				}
			}
		}
		
		return m;
	}
}
