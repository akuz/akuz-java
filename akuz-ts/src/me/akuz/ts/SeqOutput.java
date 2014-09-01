package me.akuz.ts;

import me.akuz.core.Out;
import me.akuz.ts.sync.Synchronizable;

public final class SeqOutput<T extends Comparable<T>>
implements Synchronizable<T> {

	private final SeqCursor<T> _seqCursor;
	private T _currTime;
	private final Seq<T> _outputSeq;
	
	public SeqOutput(final SeqCursor<T> seqCursor) {
		_seqCursor = seqCursor;
		_outputSeq = new Seq<>();
	}
	
	public Seq<T> getSeq() {
		return _outputSeq;
	}
	
	@Override
	public T getCurrTime() {
		CurrTime.checkSet(_currTime);
		return _currTime;
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {
		return _seqCursor.getNextTime(nextTime);
	}
	
	@Override
	public void moveToTime(final T time) {

		CurrTime.checkNew(_currTime, time);

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
