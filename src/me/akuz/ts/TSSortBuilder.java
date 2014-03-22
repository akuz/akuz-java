package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds time series from unsorted data.
 *
 * @param <T> - Time type.
 */
public class TSSortBuilder<T extends Comparable<T>> {

	private final List<TSEntry<T>> _queue;
	
	public TSSortBuilder() {
		_queue = new ArrayList<>();
	}
	
	public void add(TSEntry<T> entry) {
		_queue.add(entry);
	}
	
	public TS<T> build() {
		List<TSEntry<T>> sorted = new ArrayList<>(_queue);
		if (sorted.size() > 1) {
			Collections.sort(sorted);
		}
		TS<T> ts = new TS<>();
		for (int i=0; i<sorted.size(); i++) {
			TSEntry<T> curr = sorted.get(i);
			if (i > 0) {
				TSEntry<T> prev = sorted.get(i-1);
				int prevTimeCmp = prev.getTime().compareTo(curr.getTime());
				if (prevTimeCmp > 0) {
					throw new IllegalStateException("Prev time (" + prev.getTime() + ") is later than curr time (" + curr.getTime() +")");
				} else if (prevTimeCmp == 0) {
					throw new IllegalStateException("Duplicate entries for the same time (" + curr.getTime() + "), cannot guarantee correct order");
				}
			}
			ts.add(curr);
		}
		return ts;
	}
}
