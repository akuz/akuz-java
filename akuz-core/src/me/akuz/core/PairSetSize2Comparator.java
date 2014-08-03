package me.akuz.core;

import java.util.Comparator;
import java.util.Set;

public class PairSetSize2Comparator<T1,T2> implements Comparator<Pair<T1,Set<T2>>> {

	private final SortOrder _sortOrder;
	
	public PairSetSize2Comparator(SortOrder sortOrder) {
		_sortOrder = sortOrder;
	}
	
	public int compare(Pair<T1, Set<T2>> arg0, Pair<T1, Set<T2>> arg1) {
		int cmp = 0;
		if (arg0.v2().size() < arg1.v2().size()) {
			cmp = -1;
		} else if (arg0.v2().size() > arg1.v2().size()) {
			cmp = 1;
		}
		if (_sortOrder.equals(SortOrder.Desc)) {
			cmp = -cmp;
		}
		return cmp;
	}

}
