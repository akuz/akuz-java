package me.akuz.ts.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.ts.TFrame;
import me.akuz.ts.TSeq;

/**
 * Describes time series IO map by match map
 * between keys, field names and data types.
 *
 */
public final class IOMap<K> {
	
	private final String   _timeFieldName;
	private final IOType _timeDataType;

	private final List<K>  _keys;
	private final List<K>  _keysReadOnly;
	
	private final Map<K, String>    _mapKeyFieldName;
	private final Map<K, IOType>  _mapKeyDataType;
	private final Map<String, K>    _mapFieldNameKey;

	public IOMap(
			final String timeFieldName, 
			final IOType timeDataType, 
			final TSeq<?> seq,
			final K fieldName,
			final IOType dataType) {
		
		this(timeFieldName, timeDataType);
		add(fieldName, dataType);
	}

	public IOMap(final String timeFieldName, final IOType timeDataType, TFrame<K, ?> frame, IOType dataType) {
		this(timeFieldName, timeDataType);
		final List<K> keys = frame.getKeys();
		for (int i=0; i<keys.size(); i++) {
			add(keys.get(i), dataType);
		}
	}

	public IOMap(final String timeFieldName, final IOType timeDataType) {
		if (timeFieldName == null) {
			throw new IllegalArgumentException("Time field name cannot be null");
		}
		if (timeFieldName.length() == 0) {
			throw new IllegalArgumentException("Time field name cannot be empty");
		}
		if (timeDataType == null) {
			throw new IllegalArgumentException("Time data type cannot be null");
		}
		_timeFieldName = timeFieldName;
		_timeDataType = timeDataType;
		_keys = new ArrayList<>();
		_keysReadOnly = Collections.unmodifiableList(_keys);
		_mapKeyFieldName = new HashMap<>();
		_mapKeyDataType = new HashMap<>();
		_mapFieldNameKey  = new HashMap<>();
	}
		
	public String getTimeFieldName() {
		return _timeFieldName;
	}
	
	public IOType getTimeDataType() {
		return _timeDataType;
	}
	
	public List<K> getKeys() {
		return _keysReadOnly;
	}
	
	public K getKey(String fieldName) {
		return _mapFieldNameKey.get(fieldName);
	}
	
	public String getFieldName(K key) {
		return _mapKeyFieldName.get(key);
	}
	
	public IOType getDataType(K key) {
		return _mapKeyDataType.get(key);
	}
	
	/**
	 * Add field for the key with default name key.toString().
	 */
	public void add(final K key, final IOType dataType) {
		add(key, key.toString(), dataType);
	}
	
	/**
	 * Add field for the key.
	 */
	public void add(final K key, final String fieldName, final IOType dataType) {
		
		if (fieldName == null) {
			throw new IllegalArgumentException("Field name cannot be null");
		}
		if (fieldName.length() == 0) {
			throw new IllegalArgumentException("Field name cannot be empty");
		}
		if (dataType == null) {
			throw new IllegalArgumentException("Data type cannot be null");
		}
		
		if (_mapKeyFieldName.containsKey(key)) {
			throw new IllegalStateException("Key '" + key + "' has already been added");
		}
		if (_mapFieldNameKey.containsKey(fieldName)) {
			throw new IllegalStateException("Field '" + fieldName + "' has already been added");
		}
		
		_keys.add(key);
		_mapKeyFieldName.put(key, fieldName);
		_mapKeyDataType.put(key, dataType);
		_mapFieldNameKey.put(fieldName, key);
	}
	
}
