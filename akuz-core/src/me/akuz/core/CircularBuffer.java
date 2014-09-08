package me.akuz.core;

import java.util.Arrays;

/**
 * Generic circular buffer implementation.
 *
 */
public final class CircularBuffer<T> {

	private final Object[] _data;
	private int _currSize;
	private int _cursor;
	
	/**
	 * Create a circular buffer with
	 * a predefined maximum size.
	 */
	public CircularBuffer(final int maxSize) {
		_data = new Object[maxSize];
		clear();
	}
	
	/**
	 * Add new value; returns old value, if
	 * there was one coming out of the buffer.
	 */
	public T add(final T value) {
		if (value == null) {
			throw new IllegalArgumentException("CircularBuffer cannot store null values");
		}
		_cursor = (_cursor + 1) % _data.length;
		if (_currSize < _data.length) {
			_currSize++;
		}
		@SuppressWarnings("unchecked")
		final T oldValue = (T)_data[_cursor];
		_data[_cursor] = value;
		return oldValue;
	}
	
	/**
	 * Get last item added to the buffer;
	 * throws exception is buffer is empty.
	 */
	@SuppressWarnings("unchecked")
	public T getLast() {
		if (_currSize == 0) {
			throw new IllegalStateException("Cannot get last item, circular buffer is empty");
		}
		return (T)_data[_cursor];
	}
	
	/**
	 * Get maximum size of the buffer.
	 */
	public int getMaxSize() {
		return _data.length;
	}
	
	/**
	 * Get current size of the buffer.
	 */
	public int getCurrSize() {
		return _currSize;
	}

	/**
	 * Check if buffer is full.
	 */
	public boolean isFull() {
		return _currSize == _data.length;
	}
	
	/**
	 * Clear the buffer; also resets all
	 * internal pointers to null, so that
	 * there are no hanging references.
	 */
	public void clear() {
		Arrays.fill(_data, null);
		_currSize = 0;
		_cursor = -1;
	}
}
