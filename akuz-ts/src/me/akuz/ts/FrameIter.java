package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.ts.sync.Synchronizable;

/**
 * Iterator over frame, helping to align 
 * the sequences of items in time.
 * 
 */
public final class FrameIter<K, T extends Comparable<T>> implements Synchronizable<T> {

	private final Frame<K, T> _frame;
	private final List<K> _keys;
	private final List<SeqIter<T>> _seqIters;
	private T _currTime;
	private final Map<K, TItem<T>> _currItems;
	private final Map<K, List<TItem<T>>> _movedItems;
	
	/**
	 * Create frame iterator for all keys.
	 */
	public FrameIter(final Frame<K, T> frame) {

		this(frame, frame.getKeys());
	}
	
	/**
	 * Create frame iterator for specific keys.
	 */
	public FrameIter(
			final Frame<K, T> frame,
			final Collection<K> keys) {
		
		if (frame == null) {
			throw new IllegalArgumentException("Frame cannot be null");
		}
		if (keys == null) {
			throw new IllegalArgumentException("Keys cannot be null");
		}
		
		_frame = frame;
		_keys = new ArrayList<>(keys);
		_seqIters = new ArrayList<>();
		_currItems = new HashMap<>();
		_movedItems = new HashMap<>();
		for (int i=0; i<_keys.size(); i++) {
			final K key = _keys.get(i);
			final SeqIter<T> seqIter = new SeqIter<>(frame.getSeq(key));
			_seqIters.add(seqIter);
			_movedItems.put(key, seqIter.getMovedItems());
		}
	}
	
	/**
	 * Get underlying frame.
	 */
	public Frame<K, T> getFrame() {
		return _frame;
	}
	
	/**
	 * Get keys that are being aligned.
	 */
	public List<K> getKeys() {
		return _keys;
	}
	
	/**
	 * Get current iterator time.
	 */
	public T getCurrTime() {
		return _currTime;
	}
	
	/**
	 * Get items occurred *exactly* 
	 * at the current iterator time.
	 */
	public Map<K, TItem<T>> getCurrItems() {
		return _currItems;
	}
	
	/**
	 * Get items that occurred after the last
	 * iterator time up to and including the
	 * current iterator time.
	 */
	public Map<K, List<TItem<T>>> getMovedItems() {
		return _movedItems;
	}
	
	/**
	 * Move to a new time.
	 */
	@Override
	public void moveToTime(final T time) {
		
		_currItems.clear();
		for (int i=0; i<_keys.size(); i++) {
			
			final K key = _keys.get(i);
			final SeqIter<T> seqIter = _seqIters.get(i);
			
			seqIter.moveToTime(time);
			
			final TItem<T> currItem = seqIter.getCurrItem();
			if (currItem != null) {
				_currItems.put(key, currItem);
			}
			_movedItems.put(key, seqIter.getMovedItems());
		}
		_currTime = time;
	}
}
