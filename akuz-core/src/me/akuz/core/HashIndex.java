package me.akuz.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Index interface using a 
 * HashMap as an underlying store of inverse links.
 * 
 */
public final class HashIndex<T> implements Index<T> {

	private final List<T> _list;
	private final Map<T, Integer> _map;

	/**
	 * Create an empty index.
	 * 
	 */
	public HashIndex() {
		_list = new ArrayList<T>();
		_map = new HashMap<T, Integer>();
	}
	
	/**
	 * Create an index and add all items from the specified collection.
	 * 
	 */
	public HashIndex(final Collection<T> collection) {
		_list = new ArrayList<T>(collection.size());
		_map = new HashMap<T, Integer>(collection.size());
		for (T item : collection) {
			ensure(item);
		}
	}
	
	@Override
	public Integer ensure(T value) {
		Integer index = _map.get(value);
		if (index == null) {
			index = _list.size();
			_map.put(value, index);
			_list.add(value);
		}
		return index;
	}

	@Override
	public void ensureAll(Collection<T> values) {
		for (T value : values) {
			ensure(value);
		}
	}
	
	@Override
	public T ensureGetCachedValue(T value) {
		Integer index = _map.get(value);
		if (index == null) {
			index = _list.size();
			_map.put(value, index);
			_list.add(value);
			return value;
		} else {
			return _list.get(index);
		}
	}
	
	@Override
	public int size() {
		return _list.size();
	}
	
	@Override
	public List<T> getList() {
		return _list;
	}
	
	@Override
	public Map<T, Integer> getMap() {
		return _map;
	}
	
	@Override
	public Integer getIndex(T value) {
		return _map.get(value);
	}
	
	@Override
	public T getValue(int index) {
		return _list.get(index);
	}
}
