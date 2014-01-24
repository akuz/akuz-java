package me.akuz.core;

import java.util.Comparator;
import java.util.List;

/**
 * Implements heap sort algorithm.
 * 
 * Benefit 1: O(n*log(n)) worst case performance,
 * compared to O(n^2) worst case performance for quick sort.
 * 
 * Benefit 2: Oracle Java implementation allocates
 * a new array for sorting (to avoid performance problems 
 * if List is a LinkedList), but this implementation
 * will do sorting in-place (no using more memory). 
 * This will cause bad performance for LinkedList, 
 * but one shouldn't use this implementation with 
 * a LinkedList.
 *
 */
public final class HeapSort {
	
	/**
	 * Sort list using natural ordering of the items.
	 * 
	 */
	public static final <T extends Comparable<T>> void sort(List<T> list) {
		sort(list, new ComparableComparator<T>(SortOrder.Asc));
	}
	
	/**
	 * Sort list using natural ordering of the items in a given sort order.
	 * 
	 */
	public static final <T extends Comparable<T>> void sort(List<T> list, SortOrder sortOrder) {
		sort(list, new ComparableComparator<T>(sortOrder));
	}

	/**
	 * Sort list using the provided items comparator.
	 * 
	 */
	public static final <T> void sort(List<T> list, Comparator<T> comparator) {

		// check if need to sort
		if (list.size() < 2) {
			return;
		}
		
		// heapify the list
		for (int i=calcParentIndex(list.size()-1); i>=0; i--) {
			siftDown(list, comparator, i, list.size());
		}
		
		// perform heap sort
		for (int i=list.size()-1; i>0; i--) {
			
			// record tree head
			{
				T tmp = list.get(i);
				list.set(i, list.get(0));
				list.set(0, tmp);
			}
			
			// ensure heap property
			siftDown(list, comparator, 0, i);
		}
		
	}
	
	private static final <T> void siftDown(List<T> list, Comparator<T> comparator, int rootIndex, int length) {
		
		int index = rootIndex;
		while (true) {
			
			int leftChildIndex = calcLeftChildIndex(index);
			if (leftChildIndex >= length) {
				
				// no left child,
				// so we reached
				// bottom of tree
				break;
			}
			
			int rightChildIndex = calcRightChildIndex(index);
			if (rightChildIndex >= length) {
				
				// no right child, so we will
				// compare with left child only
				T parent = list.get(index);
				T child = list.get(leftChildIndex);
				if (comparator.compare(parent, child) < 0) {
					
					// exchange nodes
					list.set(index, child);
					list.set(leftChildIndex, parent);
				}
				
				// finish, as this is 
				// the last level because
				/// there is no right child
				break;
			}
			
			// choose largest index
			int largestIndex = index;
			{
				T largest = list.get(largestIndex);
				T leftChild = list.get(leftChildIndex);
				if (comparator.compare(largest, leftChild) < 0) {
					largestIndex = leftChildIndex;
				}
			}
			{
				T largest = list.get(largestIndex);
				T rightChild = list.get(rightChildIndex);
				if (comparator.compare(largest, rightChild) < 0) {
					largestIndex = rightChildIndex;
				}
			}
			
			// check if need to exchange
			if (index != largestIndex) {
				
				// exchange nodes
				T tmp = list.get(index);
				list.set(index, list.get(largestIndex));
				list.set(largestIndex, tmp);
				
				// go deeper
				index = largestIndex;

			} else {
				
				// sifted
				break;
			}
		}
	}

	private static final int calcParentIndex(int index) {
		return (index-1) / 2; // integer division
	}

	private static final int calcLeftChildIndex(int index) {
		return 2 * index + 1;
	}

	private static final int calcRightChildIndex(int index) {
		return 2 * index + 2;
	}

}
