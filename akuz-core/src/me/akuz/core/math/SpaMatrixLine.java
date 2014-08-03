package me.akuz.core.math;

import java.util.HashMap;
import java.util.Map;

public final class SpaMatrixLine {
	
	private final int[] _indices;
	private final double[] _values;
	private final Map<Integer, Integer> _idxByIndex;
	
	public SpaMatrixLine(int length) {
		_indices = new int[length];
		_values = new double[length];
		_idxByIndex = new HashMap<Integer, Integer>(length);
	}
	
	public SpaMatrixLine(SpaMatrixLine copyFrom) {
		if (copyFrom == null) {
			throw new NullPointerException("copyFrom");
		}
		_indices = copyFrom._indices.clone();
		_values = copyFrom._values.clone();
		_idxByIndex = new HashMap<Integer, Integer>(_values.length);
		_idxByIndex.putAll(copyFrom._idxByIndex);
	}
	
	public final int size() {
		return _indices.length;
	}
	
	public final boolean set(int index, double value) {
		Integer idx = _idxByIndex.get(index);
		if (idx == null) {
			return false;
		} else {
			_values[idx] = value;
			return true;
		}
	}

	public final double get(int index) {
		Integer idx = _idxByIndex.get(index);
		return idx == null ? 0.0 : _values[idx];
	}
	
	public final void set(int idx, int index, double value) {
		_indices[idx] = index;
		_values[idx] = value;
		_idxByIndex.put(index, idx);
	}
	
	public final int getIndexByIdx(int idx) {
		return _indices[idx];
	}
	
	public final double getValueByIdx(int idx) {
		return _values[idx];
	}
}
