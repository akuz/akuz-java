package me.akuz.core;

import java.util.Comparator;

/**
 * Implements comparator based on the natural ordering of elements; useful for 
 * writing the same algorithm code for both, natural and unnatural, orderings.
 *
 */
public final class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {

	private final int _sortOrderMultiplier;
	
	public ComparableComparator(SortOrder sortOrder) {
		if (sortOrder.equals(SortOrder.Asc)) {
			_sortOrderMultiplier = +1;
		} else if (sortOrder.equals(SortOrder.Desc)) {
			_sortOrderMultiplier = -1;
		} else {
			throw new IllegalArgumentException("Unsupported sort order: " + sortOrder);
		}
	}
	
	@Override
	public int compare(T o1, T o2) {
		return _sortOrderMultiplier * o1.compareTo(o2);
	}
}
