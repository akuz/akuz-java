package me.akuz.ts.sync;

import java.util.ArrayList;
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
	private T _currTime;
	
	public Synchronizer() {
		_synchronizables = new ArrayList<>();
	}
	
	public void add(Synchronizable<T> synchronizable) {
		_synchronizables.add(synchronizable);
		if (_currTime != null) {
			synchronizable.moveToTime(_currTime);
		}
	}
	
	public void moveToTime(T time) {
		if (time == null) {
			throw new IllegalArgumentException(
					"Time cannot be null");
		}
		if (_currTime != null) {
			if (_currTime.compareTo(time) >= 0) {
				throw new IllegalArgumentException(
						"Moving in time must be " +
						"strictly chronological" +
						"; current synch time: " + _currTime + 
						", requested move to time: " + time);
			}
		}
		for (int i=0; i<_synchronizables.size(); i++) {
			_synchronizables.get(i).moveToTime(time);
		}
		_currTime = time;
	}
}
