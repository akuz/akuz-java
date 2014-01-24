package me.akuz.core.sort;

import java.util.Comparator;
import java.util.List;

import me.akuz.core.ComparableComparator;
import me.akuz.core.SortOrder;

/**
 * "Binary Search" algorithm implementation, 
 * allowing to specify explicit list search boundaries;
 * useful for in-line sorting algorithms using parts of lists.
 *
 */
public final class BinarySearch {

	public static final <T extends Comparable<T>> int find(List<T> list, T key) {
		return find(list, 0, list.size(), key, new ComparableComparator<T>(SortOrder.Asc));
	}

	public static final <T extends Comparable<T>> int find(List<T> list, int start, int end, T key) {
		return find(list, start, end, key, new ComparableComparator<T>(SortOrder.Asc));
	}

	public static final <T extends Comparable<T>> int find(List<T> list, T key, SortOrder sortOrder) {
		return find(list, 0, list.size(), key, new ComparableComparator<T>(sortOrder));
	}

	public static final <T extends Comparable<T>> int find(List<T> list, int start, int end, T key, SortOrder sortOrder) {
		return find(list, start, end, key, new ComparableComparator<T>(sortOrder));
	}

	public static final <T> int find(final List<T> list, final int start, final int end, final T key, final Comparator<T> comparator) {
		
		// check parameters
		if (start > end) {
			throw new IllegalArgumentException("Start (" + start + ") must be <= end (" + end + ")");
		}
		
		// check if empty list
		if (start == end) {
			return -1 -start;
		}
		
		int left = start;
		int right = end-1;
		
		while (left < right) {
			
			// find middle
			final int mid = (left + right) / 2; // integer division
			
			// compare to mid key
			final T midKey = list.get(mid);
			final int cmp = comparator.compare(key, midKey);
			
			if (cmp == 0) {
				
				// found the key
				return mid;
			
			} else if (cmp > 0) {
				
				// key is larger than mid key,
				// dig into right block
				left = mid + 1;
				
			} else if (cmp < 0) {
				
				// key is smaller than mid key,
				// dig into left block
				right = mid - 1;
			}
		}
		
		// left == right now,
		// compare to left key
		final T leftKey = list.get(left);
		final int cmp = comparator.compare(key, leftKey);
		
		if (cmp == 0) {
			
			// found the key
			return left;
		
		} else if (cmp > 0) {
			
			// key is larger than mid key,
			// return insertion after it
			final int insertAt = left + 1;
			return -1 -insertAt;
			
		} else if (cmp < 0) {
			
			// key is smaller than mid key,
			// return insertion before it
			final int insertAt = left;
			return -1 -insertAt;
		}
		
		throw new InternalError("Internal error in binary search");
	}
}
