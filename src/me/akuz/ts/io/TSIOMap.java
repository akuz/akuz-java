package me.akuz.ts.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.akuz.ts.TSEntry;

import org.json.JSONObject;

/**
 * Describes time series IO map.
 *
 */
public final class TSIOMap<K, T extends Comparable<T>> {
	
	private final Map<K, String> _keyNameMap;
	private final Map<K, TSIOType> _keyTypeMap;
	private final Map<String, K> _nameKeyMap;
	private final Map<String, TSIOType> _nameTypeMap;

	public TSIOMap() {
		_keyNameMap  = new HashMap<>();
		_keyTypeMap  = new HashMap<>();
		_nameKeyMap  = new HashMap<>();
		_nameTypeMap = new HashMap<>();
	}
	
	public void add(final K key, final TSIOType type) {
		add(key, key.toString(), type);
	}
	
	public void add(final K key, final String name, final TSIOType type) {
		
		if (_keyNameMap.containsKey(key)) {
			throw new IllegalStateException("Key " + key + " has already been added");
		}
		if (_nameKeyMap.containsKey(name)) {
			throw new IllegalStateException("Name " + key + " has already been added");
		}
		_keyNameMap.put(key, name);
		_keyTypeMap.put(key, type);
		_nameKeyMap.put(name, key);
		_nameTypeMap.put(name, type);
	}
	
	public Set<K> getKeys() {
		return _keyNameMap.keySet();
	}
	
	public Set<String> getNames() {
		return _nameKeyMap.keySet();
	}
	
	public String getName(K key) {
		return _keyNameMap.get(key);
	}
	
	public K getKey(String name) {
		return _nameKeyMap.get(name);
	}
	
	public Object fromJson(JSONObject obj, String name) throws IOException {
		
		if (!obj.has(name)) {
			return null;
		}
		
		TSIOType type = _nameTypeMap.get(name);
		if (type == null) {
			return null;
		}
		
		return type.fromJson(obj, name);
	}
	
	public void setJsonField(TSEntry<T> entry, K key, JSONObject obj) {
		
		String name = _keyNameMap.get(key);
		if (name == null) {
			return;
		}
		
		TSIOType type = _keyTypeMap.get(key);
		type.setJsonField(obj, name, entry.getObject());
	}
	
}
