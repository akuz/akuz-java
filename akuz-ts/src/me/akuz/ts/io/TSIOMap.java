package me.akuz.ts.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.akuz.ts.TItem;
import me.akuz.ts.TFrame;
import me.akuz.ts.io.types.TSIOType;

import com.google.gson.JsonObject;

/**
 * Describes time series IO map by match map
 * between keys, field names and data types.
 *
 */
public final class TSIOMap<K, T extends Comparable<T>> {
	
	private final Map<K, String> _keyFieldNameMap;
	private final Map<K, TSIOType> _keyDataTypeMap;
	private final Map<String, K> _fieldNameKeyMap;
	private final Map<String, TSIOType> _fieldNameDataTypeMap;

	public TSIOMap() {
		_keyFieldNameMap  = new HashMap<>();
		_keyDataTypeMap  = new HashMap<>();
		_fieldNameKeyMap  = new HashMap<>();
		_fieldNameDataTypeMap = new HashMap<>();
	}

	public TSIOMap(TFrame<K, T> frame, TSIOType dataType) {
		this();
		for (K key : frame.getMap().keySet()) {
			add(key, dataType);
		}
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
		_keyFieldNameMap.put(key, fieldName);
		_keyDataTypeMap.put(key, dataType);
		_fieldNameKeyMap.put(fieldName, key);
		_fieldNameDataTypeMap.put(fieldName, dataType);
	}
	
	public Set<K> getKeys() {
		return _keyFieldNameMap.keySet();
	}
	
	public Set<String> getFieldNames() {
		return _fieldNameKeyMap.keySet();
	}
	
	public String getFieldName(K key) {
		return _keyFieldNameMap.get(key);
	}
	
	public K getKey(String fieldName) {
		return _fieldNameKeyMap.get(fieldName);
	}
	
	public Object fromJson(JsonObject obj, String fieldName) throws IOException {
		
		if (!obj.has(fieldName)) {
			return null;
		}
		
		TSIOType dataType = _fieldNameDataTypeMap.get(fieldName);
		if (dataType == null) {
			return null;
		}
		
		return dataType.fromJson(obj, fieldName);
	}
	
	public void setJsonField(TItem<T> entry, K key, JsonObject obj) {
		
		String fieldName = _keyFieldNameMap.get(key);
		if (fieldName == null) {
			return;
		}
		
		TSIOType dataType = _keyDataTypeMap.get(key);
		dataType.setJsonField(obj, fieldName, entry.getObject());
	}
	
}
