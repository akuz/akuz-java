package me.akuz.ts;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Out;
import me.akuz.ts.sync.Synchronizable;

/**
 * {@link Seq} iterator.
 *
 */
public final class SeqIterator<T extends Comparable<T>> 
implements Synchronizable<T>, SeqCursor<T>, Cloneable {
	
	private final Seq<T> _seq;
	private int _nextCursor;
	private T _currTime;
	private TItem<T> _currItem;
	private final List<TItem<T>> _movedItems;
	
	public SeqIterator(final Seq<T> seq) {
		if (seq == null) {
			throw new IllegalArgumentException("Cannot iterate over null sequence");
		}
		_seq = seq;
		_movedItems = new ArrayList<>();
	}
	
	public Seq<T> getSeq() {
		return _seq;
	}
	
	@Override
	public T getCurrTime() {
		CurrTime.checkSet(_currTime);
		return _currTime;
	}
	
	@Override
	public TItem<T> getCurrItem() {
		CurrTime.checkSet(_currTime);
		return _currItem;
	}
	
	public List<TItem<T>> getMovedItems() {
		CurrTime.checkSet(_currTime);
		return _movedItems;
	}
	
	public int getNextCursor() {
		return _nextCursor;
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {
		
		final List<TItem<T>> items = _seq.getItems();
		if (_nextCursor < items.size()) {
			nextTime.setValue(items.get(_nextCursor).getTime());
			return true;
		} else {
			nextTime.setValue(null);
			return false;
		}
	}

	@Override
	public void moveToTime(final T time) {

		CurrTime.checkNew(_currTime, time);
		
		_currItem = null;
		_movedItems.clear();
		
		final List<TItem<T>> items = _seq.getItems();
		
		// move to new time
		// item by item
		while (true) {
			
			if (_nextCursor >= items.size()) {
				
				// reached the end of 
				// available items
				break;
				
			} else {
				
				// get next item info
				final TItem<T> nextItem = items.get(_nextCursor);
				final int cmp = nextItem.getTime().compareTo(time);

				// check if next item
				// is in the future
				if (cmp > 0) {
					break;
				}
				
				// check if next item is
				// exactly at the new time
				if (cmp == 0) {
					if (_currItem != null) {
						throw new IllegalStateException(
								"Encountered two items at " +
								"the same time: " + time);
					}
					_currItem = nextItem;
				}
				
				_movedItems.add(nextItem);
				_nextCursor++;
			}
		}
		
		_currTime = time;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public SeqIterator<T> clone() {
		try {
			return (SeqIterator<T>)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Clone error");
		}
	}

}
