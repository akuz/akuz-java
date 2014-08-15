package me.akuz.ts;

import java.util.List;

import me.akuz.core.Out;
import me.akuz.ts.sync.Synchronizable;

public final class FrameTransform<K, T extends Comparable<T>> 
implements Synchronizable<T> {
	
	private final FrameCursor<K, T> _frameCursor;
	private final List<K> _frameCursorKeys;
	private T _currTime;
	private final Frame<K, T> _outputFrame;
	
	public FrameTransform(
			final FrameCursor<K, T> frameCursor) {
		
		this(frameCursor, new Frame<K, T>());
	}
	
	public FrameTransform(
			final FrameCursor<K, T> frameCursor,
			final Frame<K, T> outputFrame) {
		
		_frameCursor = frameCursor;
		_frameCursorKeys = frameCursor.getKeys();
		_outputFrame = outputFrame;
	}
	
	public Frame<K, T> getFrame() {
		return _outputFrame;
	}
	
	@Override
	public T getCurrTime() {
		return _currTime;
	}
	
	@Override
	public boolean getNextTime(Out<T> nextTime) {
		return _frameCursor.getNextTime(nextTime);
	}

	@Override
	public void moveToTime(T time) {

		if (_currTime != null) {
			final int cmp = _currTime.compareTo(time);
			if (cmp > 0)
				throw new IllegalStateException(
						"Trying to move backwards in time from " + 
						_currTime + " to " + time);
			if (cmp == 0)
				return;
		}
		
		_frameCursor.moveToTime(time);
		
		for (int i=0; i<_frameCursorKeys.size(); i++) {
			
			final K key = _frameCursorKeys.get(i);
			final TItem<T> currItem = _frameCursor.getCurrItem(key);
			if (currItem != null) {
				_outputFrame.add(key, currItem);
			}
		}
		
		_currTime = time;
	}
	
	public void runToEnd() {
		
		final Out<T> nextTime = new Out<>();
		while (getNextTime(nextTime)) {
			moveToTime(nextTime.getValue());
		}
	}

}
