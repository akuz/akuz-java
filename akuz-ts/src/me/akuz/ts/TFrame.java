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
	
	/**
	 * Contain an empty frame.
	 */
	public TFrame() {
		_keys = new ArrayList<>();
		_keysReadOnly = Collections.unmodifiableList(_keys);
		_map = new HashMap<>();
		_mapReadOnly = Collections.unmodifiableMap(_map);
	}
	
	/**
	 * Create a frame containing a sequence provided.
	 */
	public TFrame(final K key, final TSeq<T> seq) {
		this();
		addSeq(key, seq);
	}
	
	/**
	 * Create a frame with sequences from all provided frames.
	 * The keys in frames being combined must be unique across 
	 * all combined frames.
	 */
	@SafeVarargs
	public TFrame(final TFrame<K, T> ... frames) {
		this();
		if (frames != null && frames.length > 0) {
			for (int i=0; i<frames.length; i++) {
				final TFrame<K, T> frame = frames[i];
				final List<K> keys = frame.getKeys();
				for (int j=0; j<keys.size(); j++) {
					final K key = keys.get(j);
					addSeq(key, frame.getSeq(key));
				}
			}
		}
	}
	
	public void add(final K key, final T time, final Object value) {
		add(key, new TItem<>(time, value));
	}
		
	public void add(final K key, final TItem<T> item) {
		TSeq<T> seq = _map.get(key);
		if (seq == null) {
			seq = new TSeq<>();
			_keys.add(key);
			_map.put(key, seq);
		}
		seq.add(item);
	}
	
	public void stage(final K key, final T time, final Object value) {
		stage(key, new TItem<>(time, value));
	}
	
	public void stage(final K key, final TItem<T> item) {
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
	
	public void addSeq(final K key, final TSeq<T> seq) {
		if (_map.containsKey(key)) {
			throw new IllegalStateException("Sequence for key " + key + " already exists");
		}
		_keys.add(key);
		_map.put(key, seq);
	}
	
	public TSeq<T> getSeq(final K key) {
		return getSeq(key, true);
	}
	
	public TSeq<T> getSeq(final K key, final boolean required) {
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
	
	public void extractTimes(final Set<T> times) {
		for (TSeq<T> seq : _map.values()) {
			seq.extractTimes(times);
		}
	}
}
