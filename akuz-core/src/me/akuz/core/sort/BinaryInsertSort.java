package me.akuz.core.sort;

import java.util.Comparator;
import java.util.List;

import me.akuz.core.ComparableComparator;
import me.akuz.core.SortOrder;

/**
 * "Binary Insert Sort" algorithm implementation (insert sort with binary search).
 * 
 * Benefit compared to "Insert Sort": worst-case number of comparisons is only O(n*log(n)).
 * 
 * Drawback compared to "Insert Sort": extra work of O(n*log(n)) for binary searches.
 * 
 * Benefit 1: Works faster than more complex algorithms for small arrays (N ~ 100).
 * 
 * Benefit 2: Does sorting in-place, requiring no additional memory.
 * 
 * Drawback: Worst-case performance of O(n^2).
 *
 */
public final class BinaryInsertSort {

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

			// find insert position
			int insertAt;
			int foundIndex = BinarySearch.find(list, 0, i, moveItem, comparator);
			if (foundIndex >= 0) {
				insertAt = foundIndex + 1;
			} else {
				insertAt = -1 -foundIndex;
			}
			
			// shift other items
			for (int j=i; j>insertAt; j--) {
				list.set(j, list.get(j-1));
			}
			
			// move item
			list.set(insertAt, moveItem);
		}
	}
	
}
