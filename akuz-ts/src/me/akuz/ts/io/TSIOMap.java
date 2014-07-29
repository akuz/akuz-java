package me.akuz.ts.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.ts.TFrame;
import me.akuz.ts.io.types.TSIOType;

/**
 * Describes time series IO map by match map
 * between keys, field names and data types.
 *
 */
public final class TSIOMap<K> {
	
	private final List<K> _keys;
	private final List<K> _keysReadOnly;
	private final Map<K, String> _keyFieldNameMap;
	private final Map<K, TSIOType> _keyDataTypeMap;
	private final Map<String, K> _fieldNameKeyMap;

	public TSIOMap() {
		_keys = new ArrayList<>();
		_keysReadOnly = Collections.unmodifiableList(_keys);
		_keyFieldNameMap  = new HashMap<>();
		_keyDataTypeMap  = new HashMap<>();
		_fieldNameKeyMap  = new HashMap<>();
	}

	public TSIOMap(TFrame<K, ?> frame, TSIOType dataType) {
		this();
		for (K key : frame.getMap().keySet()) {
			add(key, dataType);
		}
	}
	
	public List<K> getKeys() {
		return _keysReadOnly;
	}
	
	public int size() {
		return _keysReadOnly.size();
	}
	
	public K getKey(String fieldName) {
		return _fieldNameKeyMap.get(fieldName);
	}
	
	public String getFieldName(K key) {
		return _keyFieldNameMap.get(key);
	}
	
	public TSIOType getDataType(K key) {
		return _keyDataTypeMap.get(key);
	}
	
	public void add(final K key, final TSIOType dataType) {
		add(key, key.toString(), dataType);
	}
	
	public void add(final K key, final String fieldName, final TSIOType dataType) {
		
		if (_keyFieldNameMap.containsKey(key)) {
			throw new IllegalStateException("Key '" + key + "' has already been added");
		}
		if (_fieldNameKeyMap.containsKey(fieldName)) {
			throw new IllegalStateException("Field name '" + fieldName + "' has already been added");
		}
		_keys.add(key);
		_keyFieldNameMap.put(key, fieldName);
		_keyDataTypeMap.put(key, dataType);
		_fieldNameKeyMap.put(fieldName, key);
	}
	
}
