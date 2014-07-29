package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Time series cube.
 *
 * @param <K1> - Level 1 key type.
 * @param <K2> - Level 2 key type.
 * @param <T> - Time type.
 */
public final class TCube<K1, K2, T extends Comparable<T>> {

	private final List<K1> _keys;
	private final List<K1> _keysReadOnly;
	private final Map<K1, TFrame<K2, T>> _map;
	private final Map<K1, TFrame<K2, T>> _mapReadOnly;
	
	public TCube() {
		_keys = new ArrayList<>();
		_keysReadOnly = Collections.unmodifiableList(_keys);
		_map = new HashMap<>();
		_mapReadOnly = Collections.unmodifiableMap(_map);
	}

	public void add(K1 key1, K2 key2, T time, Object value) {
		add(key1, key2, new TItem<>(time, value));
	}

	public void add(K1 key1, K2 key2, TItem<T> item) {
		TFrame<K2, T> frame = _map.get(key1);
		if (frame == null) {
			frame = new TFrame<>();
			_keys.add(key1);
			_map.put(key1, frame);
		}
		frame.add(key2, item);
	}
	
	public void stage(K1 key1, K2 key2, T time, Object value) {
		stage(key1, key2, new TItem<>(time, value));
	}
	
	public void stage(K1 key1, K2 key2, TItem<T> item) {
		TFrame<K2, T> frame = _map.get(key1);
		if (frame == null) {
			frame = new TFrame<>();
			_keys.add(key1);
			_map.put(key1, frame);
		}
		frame.stage(key2, item);
	}
	
	public void acceptStaged() {
		for (TFrame<K2, T> frame: _map.values()) {
			frame.acceptStaged();
		}
	}
	
	public void clearStaged() {
		for (TFrame<K2, T> frame: _map.values()) {
			frame.clearStaged();
		}
	}
		
	public void addFrame(K1 key, TFrame<K2, T> frame) {
		if (_map.containsKey(key)) {
			throw new IllegalStateException("Frame for key '" + key + "' already exists");
		}
		_keys.add(key);
		_map.put(key, frame);
	}
	
	public TFrame<K2, T> getFrame(K1 key) {
		return getFrame(key, true);
	}
	
	public TFrame<K2, T> getFrame(K1 key, boolean required) {
		TFrame<K2, T> frame = _map.get(key);
		if (frame == null && required) {
			throw new IllegalStateException("Frame for key '" + key + "' does not exist");
		}
		return frame;
	}
	
	public List<K1> getKeys() {
		return _keysReadOnly;
	}
	
	public Map<K1, TFrame<K2, T>> getMap() {
		return _mapReadOnly;
	}
	
	public TCube<K2, K1, T> reshuffle() {
		
		TCube<K2, K1, T> resultCube = new TCube<>();

		for (int i=0; i<_keys.size(); i++) {
			
			final K1 key1 = _keys.get(i);
			final TFrame<K2, T> frame1 = _map.get(key1);
			
			List<K2> keys2 = frame1.getKeys();
			for (int j=0; j<keys2.size(); j++) {
				
				final K2 key2 = keys2.get(j);
				final TSeq<T> seq = frame1.getSeq(key2);
				
				TFrame<K1, T> resultFrame = resultCube.getFrame(key2, false);
				if (resultFrame == null) {
					resultFrame = new TFrame<>();
					resultCube.addFrame(key2, resultFrame);
				}
				
				resultFrame.addSeq(key1, seq);
			}
		}
		return resultCube;
	}
	
	public void extractTimes(Set<T> times) {
		for (TFrame<K2, T> frame : _map.values()) {
			for (TSeq<T> seq : frame.getMap().values()) {
				seq.extractTimes(times);
			}
		}
	}
}
