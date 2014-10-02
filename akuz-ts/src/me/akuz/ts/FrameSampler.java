package me.akuz.ts;

import java.util.List;

import me.akuz.core.Out;
import me.akuz.ts.sync.TimeSchedule;

/**
 * Allows sampling underlying frame cursor 
 * at specific points in time (to which the cursor
 * is moved to), and collecting the results into
 * a new frame, which can be obtained using
 * getResult().
 * 
 */
public final class FrameSampler<K, T extends Comparable<T>> 
implements TimeSchedule<T> {
	
	private final FrameCursor<K, T> _frameCursor;
	private final boolean _moveCursor;
	private final List<K> _keys;
	private T _currTime;
	private final Frame<K, T> _result;
	
	public FrameSampler(final FrameCursor<K, T> frameCursor) {
		this(frameCursor, true);
	}
	
	public FrameSampler(final FrameCursor<K, T> frameCursor, final boolean moveCursor) {
		
		_frameCursor = frameCursor;
		_moveCursor = moveCursor;
		_keys = frameCursor.getKeys();
		_result = new Frame<K, T>();
	}
	
	/**
	 * Get the result frame (sampled).
	 */
	public Frame<K, T> getResult() {
		return _result;
	}
	
	/**
	 * Run the sampler through all times
	 * in the underlying cursor and collect
	 * all samples from the cursor.
	 */
	public Frame<K, T> runToEnd() {
		if (!_moveCursor) {
			throw new IllegalStateException(
					"Sampler is set not to move the underlying " +
					"cursor, and so it cannot run to the end.");
		}
		final Out<T> nextTime = new Out<>();
		while (getNextTime(nextTime)) {
			moveToTime(nextTime.getValue());
		}
		return _result;
	}
	
	@Override
	public T getCurrTime() {
		CurrTime.checkSet(_currTime);
		return _currTime;
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {
		return _frameCursor.getNextTime(nextTime);
	}

	@Override
	public void moveToTime(final T time) {

		CurrTime.checkNew(_currTime, time);
		
		if (_moveCursor) {
			_frameCursor.moveToTime(time);
		}
		
		for (int i=0; i<_keys.size(); i++) {
			
			final K key = _keys.get(i);
			final TItem<T> currItem = _frameCursor.getCurrItem(key);
			if (currItem != null) {
				_result.add(key, currItem);
			}
		}
		
		_currTime = time;
	}

}
