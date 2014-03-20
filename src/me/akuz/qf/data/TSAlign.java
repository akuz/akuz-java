package me.akuz.qf.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Index;
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

	public <K> Matrix intoMatrix(Index<K> colsIndex, Map<K, TS<T>> map) {
		
		if (colsIndex == null) {
			throw new IllegalArgumentException("Argument colsIndex must not be null");
		}
		
		final int[] jCursors = new int[colsIndex.size()];
		final Matrix m = new Matrix(_times.size(), colsIndex.size(), Double.NaN);
		
		for (int i=0; i<_times.size(); i++) {
			
			T currTime = _times.get(i);

			for (int j=0; j<colsIndex.size(); j++) {
				
				final TS<T> jTS = map.get(colsIndex.getValue(j));
				if (jTS != null) {
					
					final List<TSEntry<T>> jSorted = jTS.getSorted();
					
					while (jCursors[j] < jSorted.size()) {
						
						final TSEntry<T> entry = jSorted.get(jCursors[j]);
						
						final int cmp = entry.getTime().compareTo(currTime);

						if (cmp > 0) {
							// not reached current time, don't move
							break;
						}
						if (cmp < 0) {
							// before current time, move forward
							jCursors[j]++;
							continue;
						}
						
						// at current time, set matrix value
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
