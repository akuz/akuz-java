package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds time series from unsorted items.
 *
 * @param <T> - Time type.
 */
public final class TSSort<T extends Comparable<T>> {

	private final List<TSItem<T>> _queue;
	
	public TSSort() {
		_queue = new ArrayList<>();
	}
	
	public void add(TSItem<T> items) {
		_queue.add(items);
	}
	
	public TS<T> build() {
		List<TSItem<T>> sorted = new ArrayList<>(_queue);
		if (sorted.size() > 1) {
			Collections.sort(sorted);
		}
		TS<T> ts = new TS<>();
		for (int i=0; i<sorted.size(); i++) {
			TSItem<T> curr = sorted.get(i);
			if (i > 0) {
				TSItem<T> prev = sorted.get(i-1);
				int prevTimeCmp = prev.getTime().compareTo(curr.getTime());
				if (prevTimeCmp > 0) {
					throw new IllegalStateException("Prev item time (" + prev.getTime() + ") is later than curr item time (" + curr.getTime() +")");
				} else if (prevTimeCmp == 0) {
					throw new IllegalStateException("Duplicate items for the same time (" + curr.getTime() + "), cannot guarantee correct order");
				}
			}
			ts.add(curr);
		}
		return ts;
	}
}
