package me.akuz.core;

import java.util.Collection;
import java.util.Comparator;

public final class PairCollSizeComparator<T1, T2 extends Collection<?>> implements Comparator<Pair<T1, T2>> {

	private final SortOrder _sortOrder;
	
	public PairCollSizeComparator(SortOrder sortOrder) {
		_sortOrder = sortOrder;
	}
	
	public int compare(Pair<T1, T2> arg0, Pair<T1, T2> arg1) {
		int cmp = arg0.v2().size() - arg1.v2().size();
		if (_sortOrder.equals(SortOrder.Asc)) {
			return cmp;
		} else if (_sortOrder.equals(SortOrder.Desc)) {
			return -cmp;
		} else {
			throw new IllegalStateException("Unknown sort order: " + _sortOrder);
		}
	}
}
