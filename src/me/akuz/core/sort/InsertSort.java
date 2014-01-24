package me.akuz.core.sort;

import java.util.Comparator;
import java.util.List;

import me.akuz.core.ComparableComparator;
import me.akuz.core.SortOrder;

/**
 * "Insert Sort" algorithm implementation.
 * 
 * Benefit compared to "Insert Sort with Binary Search": no overhead of extra binary search.
 * Drawback compared to "Insert Sort with Binary Search": worst-case calls to compare() is O(n^2).
 * Benefit 1: Works faster than more complex algorithms for small arrays (n ~ 100).
 * Benefit 2: Does sorting in-place, requiring no additional memory.
 * Drawback: Worst-case performance of O(n^2).
 *
 */
public final class InsertSort {

	public static final <T extends Comparable<T>> void sort(List<T> list) {
		sort(list, new ComparableComparator<T>(SortOrder.Asc));
	}

	public static final <T extends Comparable<T>> void sort(List<T> list, SortOrder sortOrder) {
		sort(list, new ComparableComparator<T>(sortOrder));
	}

	public static final <T> void sort(final List<T> list, final Comparator<T> comparator) {
		
		// check if need to sort
		if (list.size() < 2) {
			return;
		}
		
		// sift up all elements after first
		for (int i=1; i<list.size(); i++) {
			
			// moving this item
			final T moveItem = list.get(i);
			
			// compare to all left items
			for (int j=i-1; j>=0; j--) {
				
				final T leftItem = list.get(j);
				if (comparator.compare(leftItem, moveItem) > 0) {
					
					// exchange items
					list.set(j+1, leftItem);
					list.set(j, moveItem);

				} else {
					
					// move finished
					break;
				}
			}
		}
	}
	
}
