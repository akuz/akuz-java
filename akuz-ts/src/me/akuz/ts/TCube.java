package me.akuz.ts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Time series cube.
 *
 * @param <K1> - Level 1 key type.
 * @param <K2> - Level 2 key type.
 * @param <T> - Time type.
 */
public final class TCube<K1, K2, T extends Comparable<T>> {

	private final Map<K1, TFrame<K2, T>> _map;
	private final Map<K1, TFrame<K2, T>> _mapReadOnly;
	
	public TCube() {
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
	
	public Map<K1, TFrame<K2, T>> getMap() {
		return _mapReadOnly;
	}
	
	public TCube<K2, K1, T> reshuffle() {
		
		TCube<K2, K1, T> resultCube = new TCube<>();

		for (Entry<K1, TFrame<K2, T>> entry1 : _map.entrySet()) {
			
			final K1 key1 = entry1.getKey();
			final TFrame<K2, T> frame1 = entry1.getValue();
			
			for (Entry<K2, TSeq<T>> entry2 : frame1.getMap().entrySet()) {
				
				final K2 key2 = entry2.getKey();
				final TSeq<T> seq = entry2.getValue();
				
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
