package me.akuz.qf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Jama.Matrix;

import me.akuz.core.Index;
import me.akuz.core.Pair;

/**
 * Historical values container, which can align 
 * the collected values into a matrix on request.
 * The values must be added in chronological order.
 *
 * @param <K> - Keys type
 * @param <T> - Time type
 * @param <V> - Values type
 */
public final class History<K, T extends Comparable<T>, V> {
	
	private T _lastTime;
	private int _timeCounter;
	private final Map<K, List<Pair<T, V>>> _map;
	
	public History() {
		_map = new HashMap<>();
	}
	
	/**
	 * Add a new historical value for a key.
	 * 
	 * @param key
	 * @param time - Must be >= time of any previously added value (for any key).
	 * @param value
	 */
	public void add(K key, T time, V value) {
		
		if (key == null) {
			throw new NullPointerException("key");
		}
		if (time == null) {
			throw new NullPointerException("time");
		}
		if (value == null) {
			throw new NullPointerException("value");
		}
		
		if (_lastTime != null) {
			final int cmp = _lastTime.compareTo(time);
			if (cmp > 0) {
				throw new IllegalStateException("All values must be added in chronological order");
			}
			if (cmp < 0) {
				_timeCounter++;
			}
		} else {
			_timeCounter++;
		}
		
		List<Pair<T, V>> list = _map.get(key);
		if (list == null) {
			list = new ArrayList<>();
			_map.put(key, list);
		}
		
		if (list.size() > 0) {
			T lastKeyTime = list.get(list.size()-1).v1();
			if (lastKeyTime.compareTo(time) >= 0) {
				throw new IllegalStateException(
						"New values for key " + key + " is not in chronological order " +
						"(last time: " + lastKeyTime + ", new time: " + time + ", new value: " + value + ")");
			}
		}
		
		list.add(new Pair<T, V>(time, value));
		_lastTime = time;
	}
	
	/**
	 * Get time of last added value (for all keys).
	 * 
	 */
	public T getLastTime() {
		return _lastTime;
	}
	
	/**
	 * Get complete data map.
	 * 
	 */
	public Map<K, List<Pair<T, V>>> getMap() {
		return _map;
	}
	
	/**
	 * Get chronological list of values for a given key.
	 * 
	 */
	public List<Pair<T, V>> getByKey(K key) {
		return _map.get(key);
	}
	
	/**
	 * Align data into a matrix.
	 * 
	 * @param keysIndex - Must have indices for all keys added to history, these indices will be used to identify matrix columns.
	 * @param missingValue - Value to populate into the matrix, if the value for a key is missing at any time in history.
	 * @return
	 */
	public Matrix alignIntoMatrix(Index<K> keysIndex, V missingValue) {
		return alignIntoMatrix(keysIndex, missingValue, null);
	}
	
	/**
	/**
	 * Align data into a matrix.
	 * 
	 * @param keysIndex - Must have indices for all keys added to history, these indices will be used to identify matrix columns.
	 * @param missingValue - Value to populate into the matrix, if the value for a key is missing at any time in history.
	 * @param outRowTimes - If not null, historical time values will be populated into this list chronologically.
	 * @return
	 */
	public Matrix alignIntoMatrix(Index<K> keysIndex, V missingValue, List<T> outRowTimes) {
		
		if (keysIndex == null) {
			throw new NullPointerException("keysIndex");
		}
		if (missingValue == null) {
			throw new NullPointerException("missingValue");
		}
		if (outRowTimes != null && outRowTimes.size() > 0) {
			throw new IllegalArgumentException("Argument list outRowTimes must be an an empty list, or null");
		}
		
		int[] cursors = new int[_map.size()];
		int[] indices = new int[_map.size()];
		List<List<Pair<T, V>>> hists = new ArrayList<>();
		{
			int j = 0;
			for (Entry<K, List<Pair<T, V>>> entry : _map.entrySet()) {
				
				K key = entry.getKey();
				
				Integer keyIndex = keysIndex.getIndex(key);
				if (keyIndex == null) {
					throw new IllegalStateException("Key " + key + " is not in keys index");
				}
				indices[j] = keyIndex.intValue();
				
				List<Pair<T, V>> hist = entry.getValue();
				hists.add(hist);
				
				j++;
			}
		}

		Matrix m = null;
		
		if (missingValue instanceof Number) {
			m = new Matrix(_timeCounter, keysIndex.size(), ((Number) missingValue).doubleValue());
		} else {
			throw new IllegalStateException("Value " + missingValue + " is not of a Number type, so cannot set it in the matrix");
		}
		
		for (int i=0; i<m.getRowDimension(); i++) {
			
			// find curr time
			T currTime = null;
			for (int j=0; j<cursors.length; j++) {
				
				final int cursor = cursors[j];
				final List<Pair<T, V>> hist = hists.get(j);
				
				if (cursor < hist.size()) {
					T time = hist.get(cursor).v1();
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
			
			// save curr time
			if (outRowTimes != null) {
				outRowTimes.add(currTime);
			}
			
			// populate row
			for (int j=0; j<cursors.length; j++) {
				
				final int cursor = cursors[j];
				final List<Pair<T, V>> hist = hists.get(j);
				
				V value = missingValue;
				if (cursor < hist.size()) {
					Pair<T, V> pair  = hist.get(cursor);
					T time = pair.v1();
					if (time.equals(currTime)) {
						value = pair.v2();
						cursors[j]++;
					}
				}
				
				if (value instanceof Number) {
					m.set(i, indices[j], ((Number) value).doubleValue());
				} else {
					throw new IllegalStateException("Value " + value + " is not of a Number type, so cannot set it in the matrix");
				}
			}
		}
		
		return m;
	}

}
