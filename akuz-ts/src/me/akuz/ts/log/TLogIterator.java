package me.akuz.ts.log;

import me.akuz.core.Out;
import me.akuz.ts.CurrTime;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.sync.Synchronizable;

/**
 * Iterator for log entries of different levels.
 * 
 */
public final class TLogIterator<T extends Comparable<T>>
implements Synchronizable<T> {
	
	private final SeqCursor<T> _infos;
	private final SeqCursor<T> _warnings;
	private final SeqCursor<T> _errors;
	private T _currTime;
	
	public TLogIterator(final TLog<T> log) {
		
		_infos = log.getInfos().iterator();
		_warnings = log.getWarnings().iterator();
		_errors = log.getErrors().iterator();
	}
	
	public SeqCursor<T> getInfos() {
		return _infos;
	}
	
	public SeqCursor<T> getWarnings() {
		return _warnings;
	}
	
	public SeqCursor<T> getErrors() {
		return _errors;
	}

	@Override
	public T getCurrTime() {
		CurrTime.checkSet(_currTime);
		return _currTime;
	}

	@Override
	public boolean getNextTime(final Out<T> nextTime) {
		// don't serve as schedule
		return false;
	}

	@Override
	public void moveToTime(final T time) {
		CurrTime.checkNew(_currTime, time);
		
		_infos.moveToTime(time);
		_warnings.moveToTime(time);
		_errors.moveToTime(time);
	}

}
