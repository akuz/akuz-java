package me.akuz.core.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;

/**
 * Selects K items from all the items added via add(),
 * providing computational efficiency of O(N*log(K)),
 * where N is the total number of items.
 * 
 */
public final class SelectK<TKey,TValue extends Comparable<TValue>> {
	
	private final int _k;
	private final PriorityQueue<Pair<TKey, TValue>> _queue;
	
	public SelectK(SortOrder sortOrder, int k) {
		
		_k = k;
		
		if (SortOrder.Asc.equals(sortOrder)) {

			// inverse sort order queue, so that the *largest* will go out first
			_queue = new PriorityQueue<Pair<TKey, TValue>>(k, new PairComparator<TKey, TValue>(SortOrder.Desc));

		} else if (SortOrder.Desc.equals(sortOrder)) {
			
			// inverse sort order queue, so that the *smallest* will go out first
			_queue = new PriorityQueue<Pair<TKey, TValue>>(k, new PairComparator<TKey, TValue>(SortOrder.Asc));
			
		} else {
			
			throw new IllegalArgumentException("Unsupported sort order: " + sortOrder);
		}
	}
	
	public void add(Pair<TKey, TValue> pair) {

		_queue.add(pair);
		
		// remove item out of bounds
		if (_queue.size() > _k) {
			_queue.poll();
		}
	}
	
	public List<Pair<TKey, TValue>> get() {
		
		List<Pair<TKey, TValue>> list = new ArrayList<>();
		
		// collect items from the queue
		while (_queue.size() > 0) {
			list.add(_queue.poll());
		}
		
		// the queue is inverse, so reverse result
		if (list.size() > 1) {
			Collections.reverse(list);
		}
		
		return list;
	}

}
