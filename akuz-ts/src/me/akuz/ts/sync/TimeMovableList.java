package me.akuz.ts.sync;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.CurrTime;

/**
 * Allows moving multiple TimeMovable
 * objects to the same time (in the
 * order in which they were added).
 *
 */
public final class TimeMovableList<T extends Comparable<T>>
implements TimeMovable<T> {
	
	private final List<TimeMovable<T>> _timeMovables;
	private T _currTime;
	
	public TimeMovableList() {
		_timeMovables = new ArrayList<>();
	}
	
	public void add(final TimeMovable<T> timeMovable) {
		_timeMovables.add(timeMovable);
		if (_currTime != null) {
			timeMovable.moveToTime(_currTime);
		}
	}
	
	@Override
	public T getCurrTime() {
		CurrTime.checkSet(_currTime);
		return _currTime;
	}
	
	@Override
	public void moveToTime(final T time) {
		
		CurrTime.checkNew(_currTime, time);
		
		for (int i=0; i<_timeMovables.size(); i++) {
			_timeMovables.get(i).moveToTime(time);
		}
		
		_currTime = time;
	}
}
