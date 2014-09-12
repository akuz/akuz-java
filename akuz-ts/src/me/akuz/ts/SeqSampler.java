package me.akuz.ts;

import me.akuz.core.Out;
import me.akuz.ts.sync.Synchronizable;

/**
 * Allows sampling underlying sequence cursor 
 * at specific points in time (to which the cursor
 * is moved to), and collecting the results into
 * a new sequence, which can be obtained using
 * getResult().
 * 
 */
public final class SeqSampler<T extends Comparable<T>>
implements Synchronizable<T> {

	private final SeqCursor<T> _seqCursor;
	private final boolean _moveCursor;
	private T _currTime;
	private final Seq<T> _result;
	
	public SeqSampler(final SeqCursor<T> seqCursor) {
		this(seqCursor, true);
	}
	
	public SeqSampler(final SeqCursor<T> seqCursor, final boolean moveCursor) {
		_seqCursor = seqCursor;
		_moveCursor = moveCursor;
		_result = new Seq<>();
	}
	
	/**
	 * Get the result sequence (sampled).
	 */
	public Seq<T> getResult() {
		return _result;
	}
	
	/**
	 * Run the sampler through all times
	 * in the underlying cursor and collect
	 * all samples from the cursor.
	 */
	public Seq<T> runToEnd() {
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
		return _seqCursor.getNextTime(nextTime);
	}
	
	@Override
	public void moveToTime(final T time) {

		CurrTime.checkNew(_currTime, time);

		if (_moveCursor) {
			_seqCursor.moveToTime(time);
		}
		
		final TItem<T> item = _seqCursor.getCurrItem();
		
		if (item != null) {
			_result.add(item);
		}
		
		_currTime = time;
	}
}
