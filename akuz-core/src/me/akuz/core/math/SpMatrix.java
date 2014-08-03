package me.akuz.core.math;

import java.util.HashMap;
import java.util.Map;

/**
 * Sparse matrix (space-optimized; uses longs as keys internally).
 *
 */
public final class SpMatrix {
	
	private final Map<SpMatrixKey, Double> _data;
	
	public SpMatrix() {
		_data = new HashMap<>();
	}
	
	public final void add(final int i, final int j, final Double value) {
		
		SpMatrixKey key = new SpMatrixKey(i, j);
		Double currValue = _data.get(key);
		if (currValue != null) {
			_data.put(key, currValue + value);
		} else {
			_data.put(key, value);
		}
	}
	
	public final void set(final int i, final int j, final Double value) {
		
		SpMatrixKey key = new SpMatrixKey(i, j);
		_data.put(key, value);
	}
	
	public final Double remove(final int i, final int j) {
		
		SpMatrixKey key = new SpMatrixKey(i, j);
		return _data.remove(key);
	}
	
	public final Double get(final int i, final int j) {
		
		SpMatrixKey key = new SpMatrixKey(i, j);
		return _data.get(key);
	}
	
	public final void clear() {
		_data.clear();
	}
	
}
