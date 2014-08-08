package me.akuz.ts;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.sync.Synchronizable;

/**
 * {@link Seq} iterator.
 *
 */
public final class SeqIter<T extends Comparable<T>> implements Synchronizable<T> {
	
	private final List<TItem<T>> _items;
	private int _nextCursor;
	private T _currTime;
	private TItem<T> _currItem;
	private final List<TItem<T>> _movedItems;
	
	public SeqIter(final Seq<T> seq) {
		if (seq == null) {
			throw new IllegalArgumentException("Cannot iterate over null sequence");
		}
		_items = seq.getItems();
		_movedItems = new ArrayList<>();
	}
	
	public T getCurrTime() {
		return _currTime;
	}
	
	public TItem<T> getCurrItem() {
		return _currItem;
	}
	
	public List<TItem<T>> getMovedItems() {
		return _movedItems;
	}

	@Override
	public void moveToTime(T time) {

		_currItem = null;
		_movedItems.clear();
		
		// move to new time
		// item by item
		while (true) {
			
			if (_nextCursor >= _items.size()) {
				
				// reached the end of 
				// available items
				break;
				
			} else {
				
				// get next item info
				final TItem<T> nextItem = _items.get(_nextCursor);
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

}
