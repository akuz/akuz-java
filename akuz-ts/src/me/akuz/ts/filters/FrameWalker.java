package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

import me.akuz.ts.FrameIterOld;
import me.akuz.ts.Frame;
import me.akuz.ts.TItem;
import me.akuz.ts.Seq;

/**
 * Iterator over frame, helping to align 
 * the sequences of items in time.
 * 
 */
public final class FrameWalker<K, T extends Comparable<T>> implements FrameIterOld<K, T> {

	private final Frame<K, T> _frame;
	private final List<T> _times;
	private final List<K> _keys;
	private final Map<K, Integer> _cursors;
	private int _timeCursor;
	private T _currTime;
	private final Map<K, TItem<T>> _currItems;
	private final Map<K, List<TItem<T>>> _movedItems;
	
	/**
	 * Create frame iterator for all keys,
	 * to iterate over all times in the frame.
	 */
	public FrameWalker(final Frame<K, T> frame) {

		this(frame, frame.getKeys(), frame.extractTimes());
	}
	
	/**
	 * Create frame iterator for specific keys,
	 * to iterate over all times in the frame.
	 */
	public FrameWalker(
			final Frame<K, T> frame,
			final Collection<K> keys) {
		
		this(frame, keys, frame.extractTimes());
	}
	
	/**
	 * Create frame iterator for specific keys 
	 * and times to iterate over.
	 */
	public FrameWalker(
			final Frame<K, T> frame,
			final Collection<K> keys,
			final Collection<T> times) {
		
		if (frame == null) {
			throw new IllegalArgumentException("Frame cannot be null");
		}
		if (times == null) {
			throw new IllegalArgumentException("Times cannot be null");
		}
		if (keys == null) {
			throw new IllegalArgumentException("Keys cannot be null");
		}
		
		_frame = frame;
		
		if (times instanceof List<?> &&
			times instanceof RandomAccess) {
			_times = (List<T>)times;
		} else {
			_times = new ArrayList<>(times);
			if (_times.size() > 1) {
				Collections.sort(_times);
			}
		}
		if (keys instanceof List<?> &&
			keys instanceof RandomAccess) {
			_keys = (List<K>)keys;
		} else {
			_keys = new ArrayList<>(keys);
		}
		_timeCursor = -1;
		_cursors = new HashMap<>();
		for (final K key : keys) {
			_cursors.put(key, 0);
		}
		_currItems = new HashMap<>();
		_movedItems = new HashMap<>();
		for (int j=0; j<_keys.size(); j++) {
			_movedItems.put(_keys.get(j), new ArrayList<TItem<T>>());
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
	 * Check if there are more times 
	 * to iterate over.
	 */
	@Override
	public boolean hasNext() {
		return _timeCursor + 1 < _times.size();
	}
	
	/**
	 * Get current iterator time.
	 */
	@Override
	public T getCurrTime() {
		return _currTime;
	}
	
	/**
	 * Get items occurred *exactly* 
	 * at the current iterator time.
	 */
	@Override
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
	 * Move to the next iterator time.
	 */
	@Override
	public void next() {
		
		_timeCursor++;
		if (_timeCursor >= _times.size()) {
			throw new IllegalStateException("Moved beyond the end of iterator times");
		}

		_currItems.clear();
		for (int j=0; j<_keys.size(); j++) {
			_movedItems.get(_keys.get(j)).clear();
		}
		
		final T prevTime = _currTime;
		_currTime = _times.get(_timeCursor);
		
		if (_currTime == null) {
			throw new IllegalStateException(
					"Encountered null iteration time at index " + _timeCursor);
		}
		
		if (prevTime != null) {
			final int cmp = prevTime.compareTo(_currTime);
			if (cmp >= 0) {
				throw new IllegalStateException(
						"Iteration times must be in strict chronological order, but" +
						" encountered " + _currTime + " after " + prevTime + 
						" at index " + _timeCursor);
			}
		}

		for (int j=0; j<_keys.size(); j++) {
			
			final K key = _keys.get(j);
			final Seq<T> seq = _frame.getSeq(key);

			if (seq != null) {
				
				final List<TItem<T>> items = seq.getItems();
				Integer cursor = _cursors.get(key);
				TItem<T> currItem = null;
				
				while (true) {
					
					TItem<T> item = null;
					int cmp = 1;
					
					// check item time
					if (cursor < items.size()) {
						item = items.get(cursor);
						cmp = item.getTime().compareTo(_currTime);
					}

					// cursor after 
					// current time
					if (cmp > 0) {
						item = null;
						break;
					}
					
					// add to moved items
					_movedItems.get(key).add(item);

					// move cursor forward
					_cursors.put(key, ++cursor);

					if (cmp == 0) {
						currItem = item;
						break;
					}
				}

				if (currItem != null) {
					_currItems.put(key, currItem);
				}
			}
		}
	}
}
