package me.akuz.core;

import java.util.Comparator;
import java.util.Set;

public class PairSetSize1Comparator<T1,T2> implements Comparator<Pair<Set<T1>,T2>> {

	private final SortOrder _sortOrder;
	
	public PairSetSize1Comparator(SortOrder sortOrder) {
		_sortOrder = sortOrder;
	}
	
	public int compare(Pair<Set<T1>,T2> arg0, Pair<Set<T1>,T2> arg1) {
		int cmp = 0;
		if (arg0.v1().size() < arg1.v1().size()) {
			cmp = -1;
		} else if (arg0.v1().size() > arg1.v1().size()) {
			cmp = 1;
		}
		if (_sortOrder.equals(SortOrder.Desc)) {
			cmp = -cmp;
		}
		return cmp;
	}

}
