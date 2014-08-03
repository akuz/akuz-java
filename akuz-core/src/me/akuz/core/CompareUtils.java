package me.akuz.core;


public final class CompareUtils {
	
	public static <T extends Comparable<T>> int compareNullsLowest(T a0, T a1, SortOrder sortOrder) {
		if (a0 == a1) {
			return 0;
		}
		if (a0 == null) {
			return sortOrder == SortOrder.Asc ? -1 : +1;
		}
		if (a1 == null) {
			return sortOrder == SortOrder.Asc ? +1 : -1;
		}
		int cmp = a0.compareTo(a1);
		return sortOrder == SortOrder.Asc ? +cmp : -cmp;
	}
	
	public static <T extends Comparable<T>> int compareNullsHighest(T a0, T a1, SortOrder sortOrder) {
		if (a0 == a1) {
			return 0;
		}
		if (a0 == null) {
			return sortOrder == SortOrder.Asc ? +1 : -1;
		}
		if (a1 == null) {
			return sortOrder == SortOrder.Asc ? -1 : +1;
		}
		int cmp = a0.compareTo(a1);
		return sortOrder == SortOrder.Asc ? +cmp : -cmp;
	}

}
