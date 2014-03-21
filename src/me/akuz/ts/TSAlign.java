package me.akuz.ts;

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

	public <K> Matrix intoMatrix(Index<K> keysIndex, Map<K, TS<T>> map) {
		return intoMatrix(keysIndex, map, null, null, null);
	}

	public <K> Matrix intoMatrix(Index<K> keysIndex, Map<K, TS<T>> map, TSAlignFill fill, TSAlignCheck check, TSAlignLog log) {
		
		if (keysIndex == null) {
			throw new IllegalArgumentException("Argument colsIndex must not be null");
		}
		
		final int[] jCursors = new int[keysIndex.size()];
		final TSAlignFill[] jFills;
		if (fill != null) {
			jFills = new TSAlignFill[keysIndex.size()];
			for (int j=0; j<keysIndex.size(); j++) {
				jFills[j] = (TSAlignFill)fill.clone();
			}
		} else {
			jFills = null;
		}
		final TSAlignCheck[] jChecks;
		if (check != null) {
			jChecks = new TSAlignCheck[keysIndex.size()];
			for (int j=0; j<keysIndex.size(); j++) {
				jChecks[j] = (TSAlignCheck)check.clone();
			}
		} else {
			jChecks = null;
		}
		final Matrix m = new Matrix(_times.size(), keysIndex.size(), Double.NaN);
		
		for (int i=0; i<_times.size(); i++) {
			
			T currTime = _times.get(i);

			for (int j=0; j<keysIndex.size(); j++) {
				
				final K key = keysIndex.getValue(j);
				final TS<T> jTS = map.get(keysIndex.getValue(j));
				if (jTS != null) {
					
					final List<TSEntry<T>> jSorted = jTS.getSorted();
					
					double value = Double.NaN;
					
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
						value = entry.getDouble();
						jCursors[j]++;
					}

					if (jFills != null) {
						final TSAlignLogMsg msg = jFills[j].next(value);
						if (msg != null && log != null) {
							log.add(new TSAlignLogMsg(msg.getLevel(), key + " at " + currTime + ": " + msg.getText()));
						}
						value = jFills[j].get();
					}
					if (jChecks != null) {
						final TSAlignLogMsg msg = jChecks[j].next(value);
						if (msg != null && log != null) {
							log.add(new TSAlignLogMsg(msg.getLevel(), key + " at " + currTime + ": " + msg.getText()));
						}
					}
					m.set(i, j, value);
				}
			}
		}
		
		return m;
	}

}
