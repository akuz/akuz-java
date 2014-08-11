package me.akuz.ts.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Time synchronizer that propagates the same time to 
 * multiple {@link Synchronizable}s in the same order, 
 * in which they were added to the {@link Synchronizer}.
 * 
 * If a new {@link Synchronizable} is added after some
 * times have already been propagated, it's automatically
 * fast-forwarded to the current {@link Synchronizer} time.
 *
 */
public final class Synchronizer<T extends Comparable<T>> {
	
	private final List<Synchronizable<T>> _synchronizables;
	private final List<T> _times;
	private int _nextCursor;
	private T _currTime;
	
	public Synchronizer(final Collection<T> times) {
		this(times, false);
	}
	
	private Synchronizer(final Collection<T> times, final boolean timesAreAlreadySorted) {

		_synchronizables = new ArrayList<>();
		_times = new ArrayList<>(times);
		
		if (!timesAreAlreadySorted && 
			_times.size() > 1) {
			
			Collections.sort(_times);
		}
	}
	
	public void add(Synchronizable<T> synchronizable) {
		_synchronizables.add(synchronizable);
		if (_currTime != null) {
			synchronizable.moveToTime(_currTime);
		}
	}
	
	public T getCurrTime() {
		return _currTime;
	}
	
	public boolean hasNext() {
		return _nextCursor < _times.size();
	}
	
	public void next() {
		
		if (_nextCursor >= _times.size()) {
			throw new IllegalStateException(
					"There are no more times to synchronize");
		}
		
		final T nextTime = _times.get(_nextCursor);
		if (nextTime == null) {
			throw new IllegalStateException(
					"Synchronizer enountered null time " + 
					"at position " + _nextCursor + " in times list");
		}
		
		if (_currTime != null) {
			if (_currTime.compareTo(nextTime) >= 0) {
				throw new IllegalArgumentException(
						"Synchronizing times must be " +
						"strictly chronological" +
						"; detected current time: " + _currTime + 
						", detected next time: " + nextTime);
			}
		}
		
		for (int i=0; i<_synchronizables.size(); i++) {
			_synchronizables.get(i).moveToTime(nextTime);
		}
		
		_currTime = nextTime;
	}
	
	public void run() {
		while (hasNext()) {
			next();
		}
	}
}
