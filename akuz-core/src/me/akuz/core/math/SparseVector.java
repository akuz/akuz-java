package me.akuz.core.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SparseVector<TKey, TValue> {
	
	private final HashMap<TKey, Integer> _indexByKey;
	private final List<TKey> _keys;
	private final List<TValue> _values;
	
	public SparseVector() {
		_indexByKey = new HashMap<TKey, Integer>();
		_keys = new ArrayList<TKey>();
		_values = new ArrayList<TValue>();
	}
	
	public SparseVector(int initialCapacity) {
		_indexByKey = new HashMap<TKey, Integer>(initialCapacity);
		_keys = new ArrayList<TKey>(initialCapacity);
		_values = new ArrayList<TValue>(initialCapacity);
	}
	
	@SuppressWarnings("unchecked")
	public SparseVector(SparseVector<TKey, TValue> fillFrom) {
		if (fillFrom == null) {
			throw new IllegalArgumentException("Parameter fillFrom must not be null");
		}
		_indexByKey = (HashMap<TKey, Integer>)fillFrom._indexByKey.clone();
		_keys = new ArrayList<TKey>(fillFrom._keys);
		_values = new ArrayList<TValue>(fillFrom._values);
	}
	
	public final int set(TKey key, TValue value) {
		if (key == null) {
			throw new IllegalArgumentException("Key must not be null");
		}
		if (value == null) {
			throw new IllegalArgumentException("Value must not be null");
		}
		Integer index = _indexByKey.get(key);
		if (index == null) {
			index = _keys.size();
			_indexByKey.put(key, index);
			_keys.add(key);
			_values.add(value);
		} else {
			_values.set(index, value);
		}
		return index;
	}
	
	public final TValue get(TKey key) {
		if (key == null) {
			throw new IllegalArgumentException("Key must not be null");
		}
		Integer index = _indexByKey.get(key);
		return index == null ? null : _values.get(index);
	}
	
	public final int size() {
		return _values.size();
	}
	
	public final List<TKey> getKeys() {
		return _keys;
	}
	
	public final TKey getKeyByIndex(int index) {
		return _keys.get(index);
	}
	
	public final TValue getValueByIndex(int index) {
		return _values.get(index);
	}
	
	public final void removeAtIndex(int index) {
		TKey key = _keys.get(index);
		_keys.remove(index);
		_values.remove(index);
		_indexByKey.remove(key);
	}

	public void clear() {
		_indexByKey.clear();
		_keys.clear();
		_values.clear();
	}
	
}
