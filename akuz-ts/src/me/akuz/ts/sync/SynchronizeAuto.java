package me.akuz.ts.sync;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Out;

/**
 * Time synchronizer that propagates the same time to 
 * multiple {@link Synchronizable}s in the same order, 
 * in which they were added to the {@link SynchronizeAuto}.
 * 
 * If a new {@link Synchronizable} is added after some
 * times have already been propagated, it's automatically
 * fast-forwarded to the current {@link SynchronizeAuto} time.
 *
 */
public final class SynchronizeAuto<T extends Comparable<T>>
implements Synchronizable<T> {
	
	private final List<Synchronizable<T>> _synchronizables;
	private T _currTime;
	
	public SynchronizeAuto() {
		_synchronizables = new ArrayList<>();
	}
	
	public void add(Synchronizable<T> synchronizable) {
		_synchronizables.add(synchronizable);
		if (_currTime != null) {
			synchronizable.moveToTime(_currTime);
		}
	}
	
	@Override
	public T getCurrTime() {
		return _currTime;
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {

		nextTime.setValue(null);
		
		final Out<T> seqNextTime = new Out<>();
		
		for (int i=0; i<_synchronizables.size(); i++) {
			
			if (_synchronizables.get(i).getNextTime(seqNextTime)) {
				
				if (nextTime.getValue() == null ||
					nextTime.getValue().compareTo(seqNextTime.getValue()) > 0) {
					
					nextTime.setValue(seqNextTime.getValue());
				}
			}
		}
		
		return nextTime.getValue() != null;
	}
	
	@Override
	public void moveToTime(T time) {
		
		if (_currTime != null) {
			if (_currTime.compareTo(time) >= 0) {
				throw new IllegalArgumentException(
						"Synchronizing times must be " +
						"strictly chronological" +
						"; current time: " + _currTime + 
						", next time: " + time);
			}
		}
		
		for (int i=0; i<_synchronizables.size(); i++) {
			_synchronizables.get(i).moveToTime(time);
		}
		
		_currTime = time;
	}
	
	public void run() {
		Out<T> nextTime = new Out<>();
		while (getNextTime(nextTime)) {
			moveToTime(nextTime.getValue());
		}
	}
}
