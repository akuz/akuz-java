package me.akuz.ts.filters;

import me.akuz.core.Out;
import me.akuz.ts.Seq;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.sync.Synchronizable;

public final class SeqTransform<T extends Comparable<T>>
implements Synchronizable<T> {

	private final SeqCursor<T> _seqCursor;
	private T _currTime;
	private final Seq<T> _outputSeq;
	
	public SeqTransform(final SeqCursor<T> seqCursor) {
		_seqCursor = seqCursor;
		_outputSeq = new Seq<>();
	}
	
	public Seq<T> getSeq() {
		return _outputSeq;
	}
	
	@Override
	public T getCurrTime() {
		return _currTime;
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {
		return _seqCursor.getNextTime(nextTime);
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

		_seqCursor.moveToTime(time);
		
		final TItem<T> filteredItem = _seqCursor.getCurrItem();
		
		if (filteredItem != null) {
			_outputSeq.add(filteredItem);
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
