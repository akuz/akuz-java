package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Time series frame.
 *
 * @param <K> - Key type.
 * @param <T> - Time type.
 */
public final class TFrame<K, T extends Comparable<T>> {
	
	private final List<K> _keys;
	private final List<K> _keysReadOnly;
	private final Map<K, TSeq<T>> _map;
	private final Map<K, TSeq<T>> _mapReadOnly;
	
	public TFrame() {
		_keys = new ArrayList<>();
		_keysReadOnly = Collections.unmodifiableList(_keys);
		_map = new HashMap<>();
		_mapReadOnly = Collections.unmodifiableMap(_map);
	}
	
	public void add(K key, T time, Object value) {
		add(key, new TItem<>(time, value));
	}
		
	public void add(K key, TItem<T> item) {
		TSeq<T> seq = _map.get(key);
		if (seq == null) {
			seq = new TSeq<>();
			_keys.add(key);
			_map.put(key, seq);
		}
		seq.add(item);
	}
	
	public void stage(K key, T time, Object value) {
		stage(key, new TItem<>(time, value));
	}
	
	public void stage(K key, TItem<T> item) {
		TSeq<T> seq = _map.get(key);
		if (seq == null) {
			seq = new TSeq<>();
			_keys.add(key);
			_map.put(key, seq);
		}
		seq.stage(item);
	}
	
	public void acceptStaged() {
		for (TSeq<T> seq: _map.values()) {
			seq.acceptStaged();
		}
	}
	
	public void clearStaged() {
		for (TSeq<T> seq: _map.values()) {
			seq.clearStaged();
		}
	}
	
	public void addSeq(K key, TSeq<T> seq) {
		if (_map.containsKey(key)) {
			throw new IllegalStateException("Sequence for key " + key + " already exists");
		}
		_keys.add(key);
		_map.put(key, seq);
	}
	
	public TSeq<T> getSeq(K key) {
		return getSeq(key, true);
	}
	
	public TSeq<T> getSeq(K key, boolean required) {
		TSeq<T> seq = _map.get(key);
		if (seq == null && required) {
			throw new IllegalStateException("Sequence for key '" + key + "' does not exist");
		}
		return seq;
	}
	
	public List<K> getKeys() {
		return _keysReadOnly;
	}
	
	public Map<K, TSeq<T>> getMap() {
		return _mapReadOnly;
	}
	
	public void extractTimes(Set<T> times) {
		for (TSeq<T> seq : _map.values()) {
			seq.extractTimes(times);
		}
	}
}
