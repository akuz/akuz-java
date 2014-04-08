package me.akuz.core;

import java.util.Comparator;

public final class PairComparator<T1, T2 extends Comparable<T2>> implements Comparator<Pair<T1, T2>> {

	private final SortOrder _sortOrder;
	
	public PairComparator(SortOrder sortOrder) {
		_sortOrder = sortOrder;
	}
	
	public int compare(Pair<T1, T2> arg0, Pair<T1, T2> arg1) {
		int cmp = arg0.v2().compareTo(arg1.v2());
		if (_sortOrder.equals(SortOrder.Asc)) {
			return cmp;
		} else if (_sortOrder.equals(SortOrder.Desc)) {
			return -cmp;
		} else {
			throw new IllegalStateException("Unknown sort order: " + _sortOrder);
		}
	}
}
