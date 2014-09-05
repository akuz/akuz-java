package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
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
	private final Index<K> _keysIndex;
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
		_keysIndex = new HashIndex<>(keys);
		_seqIters = new ArrayList<>();
		_currItems = new HashMap<>();
		_movedItems = new HashMap<>();
		for (int i=0; i<_keysIndex.size(); i++) {
			final K key = _keysIndex.getValue(i);
			final SeqIterator<T> seqIter = new SeqIterator<>(frame.getSeq(key));
			_seqIters.add(seqIter);
		}
	}
	
	@Override
	public Frame<K, T> getFrame() {
		return _frame;
	}
	
	@Override
	public SeqCursor<T> getSeqCursor(final K key) {
		Integer index = _keysIndex.getIndex(key);
		if (index == null) {
			throw new IllegalArgumentException("Sequence for key '" + key + "' does not exist");
		}
		return _seqIters.get(index);
	}
	
	@Override
	public List<K> getKeys() {
		return _keysIndex.getList();
	}
	
	@Override
	public T getCurrTime() {
		CurrTime.checkSet(_currTime);
		return _currTime;
	}
	
	@Override
	public Map<K, TItem<T>> getCurrItems() {
		CurrTime.checkSet(_currTime);
		return _currItems;
	}
	
	@Override
	public TItem<T> getCurrItem(K key) {
		CurrTime.checkSet(_currTime);
		return _currItems.get(key);
	}
	
	@Override
	public List<TItem<T>> getMovedItems(final K key) {
		CurrTime.checkSet(_currTime);
		return _movedItems.get(key);
	}

	@Override
	public Map<K, List<TItem<T>>> getMovedItems() {
		CurrTime.checkSet(_currTime);
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
	
	@Override
	public void moveToTime(final T time) {

		CurrTime.checkNew(_currTime, time);
		
		_currItems.clear();
		for (int i=0; i<_keysIndex.size(); i++) {
			
			final K key = _keysIndex.getValue(i);
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
