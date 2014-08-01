package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Time series frame containing multiple sequences.
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
	
	/**
	 * Add value to a sequence in this frame.
	 * Sequence is created, if it doesn't exist.
	 */
	public void add(final K key, final T time, final Object value) {
		add(key, new TItem<>(time, value));
	}

	/**
	 * Add item to a sequence in this frame.
	 * Sequence is created, if it doesn't exist.
	 */
	public void add(final K key, final TItem<T> item) {
		TSeq<T> seq = _map.get(key);
		if (seq == null) {
			seq = new TSeq<>();
			_keys.add(key);
			_map.put(key, seq);
		}
		seq.add(item);
	}
	
	/**
	 * Stage value in a sequence in this frame.
	 * Sequence is created, if it doesn't exist.
	 */
	public void stage(final K key, final T time, final Object value) {
		stage(key, new TItem<>(time, value));
	}
	
	/**
	 * Stage item in a sequence in this frame.
	 * Sequence is created, if it doesn't exist.
	 */
	public void stage(final K key, final TItem<T> item) {
		TSeq<T> seq = _map.get(key);
		if (seq == null) {
			seq = new TSeq<>();
			_keys.add(key);
			_map.put(key, seq);
		}
		seq.stage(item);
	}
	
	/**
	 * Accept staged items in all sequences.
	 */
	public void acceptStaged() {
		for (TSeq<T> seq: _map.values()) {
			seq.acceptStaged();
		}
	}
	
	/**
	 * Clear staged items in all sequences.
	 */
	public void clearStaged() {
		for (TSeq<T> seq: _map.values()) {
			seq.clearStaged();
		}
	}
	
	/**
	 * Add existing sequence to this frame.
	 */
	public void addSeq(final K key, final TSeq<T> seq) {
		if (_map.containsKey(key)) {
			throw new IllegalStateException("Sequence for key " + key + " already exists");
		}
		_keys.add(key);
		_map.put(key, seq);
	}
	
	/**
	 * Get sequence from this frame.
	 * Throws if key doesn't exist.
	 */
	public TSeq<T> getSeq(final K key) {
		return getSeq(key, true);
	}
	
	/**
	 * Get sequence from this frame.
	 * Returns null if key doesn't exist and !required.
	 */
	public TSeq<T> getSeq(final K key, final boolean required) {
		TSeq<T> seq = _map.get(key);
		if (seq == null && required) {
			throw new IllegalStateException("Sequence for key '" + key + "' does not exist");
		}
		return seq;
	}
	
	/**
	 * Get keys of all sequences in this frame.
	 */
	public List<K> getKeys() {
		return _keysReadOnly;
	}
	
	/**
	 * Get key to sequence map of this frame.
	 */
	public Map<K, TSeq<T>> getMap() {
		return _mapReadOnly;
	}
	
	/**
	 * Extract times present in all sequences of this frame.
	 */
	public void extractTimes(final Set<T> times) {
		for (TSeq<T> seq : _map.values()) {
			seq.extractTimes(times);
		}
	}
}
