package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.Out;
import me.akuz.ts.sync.Synchronizable;

/**
 * Iterator over frame, performing alignment 
 * of multiple sequences of items in time.
 * 
 */
public final class FrameIterator<K, T extends Comparable<T>> 
implements Synchronizable<T>, FrameCursor<K, T> {

	private final Frame<K, T> _frame;
	private final List<K> _keys;
	private final List<SeqIterator<T>> _seqIters;
	private T _currTime;
	private final Map<K, TItem<T>> _currItems;
	private final Map<K, List<TItem<T>>> _movedItems;
	
	/**
	 * Create frame iterator for all keys.
	 */
	public FrameIterator(final Frame<K, T> frame) {

		this(frame, frame.getKeys());
	}
	
	/**
	 * Create frame iterator for specific keys.
	 */
	public FrameIterator(
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
			final SeqIterator<T> seqIter = new SeqIterator<>(frame.getSeq(key));
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
	@Override
	public List<K> getKeys() {
		return _keys;
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
	 * Get item occurred *exactly* 
	 * at the current iterator time.
	 */
	@Override
	public TItem<T> getCurrItem(K key) {
		return _currItems.get(key);
	}
	
	/**
	 * Get items that occurred after the last
	 * iterator time up to and including the
	 * current iterator time.
	 */
	public Map<K, List<TItem<T>>> getMovedItems() {
		return _movedItems;
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {

		nextTime.setValue(null);
		
		final Out<T> seqNextTime = new Out<>();
		
		for (int i=0; i<_seqIters.size(); i++) {
			
			final SeqIterator<T> seqIter = _seqIters.get(i);
			
			if (seqIter.getNextTime(seqNextTime)) {
				
				// if we haven't found next time yet
				// or the seq next time is *earlier*
				if (nextTime.getValue() == null ||
					nextTime.getValue().compareTo(seqNextTime.getValue()) > 0) {
					
					nextTime.setValue(seqNextTime.getValue());
				}
			}
		}
		
		return nextTime.getValue() != null;
	}
	
	/**
	 * Move to a new time.
	 */
	@Override
	public void moveToTime(final T time) {

		if (_currTime != null) {
			final int cmp = _currTime.compareTo(time);
			if (cmp > 0)
				throw new IllegalStateException(
						"Trying to move backwards in time from " + 
						_currTime + " to " + time);
			if (cmp == 0)
				return;
		}
		
		_currItems.clear();
		for (int i=0; i<_keys.size(); i++) {
			
			final K key = _keys.get(i);
			final SeqIterator<T> seqIter = _seqIters.get(i);
			
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
