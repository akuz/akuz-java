package me.akuz.core.sort;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import me.akuz.core.ComparableComparator;
import me.akuz.core.SortOrder;

/**
 * "Quick Sort" algorithm implementation, with following optimization:
 * detecting items equal to the pivot value, and not sorting them further.
 * 
 * Benefit: Average performance of O(n*log(n)). Often outperforms other algorithms.
 * 
 * Drawback: Worst-case performance of O(n^2).
 *
 */
public final class QuickSort {

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
		
		// recursive calls queue
		final Queue<int[]> queue = new LinkedList<>();
		queue.add(new int[] {0, list.size()-1});
		
		// execute recursive calls
		while (queue.size() > 0) {
			
			final int[] pair = queue.poll();
			final int leftIndex = pair[0];
			final int rightIndex = pair[1];
			
			// select pivot
			final int pivotIndex = leftIndex + (rightIndex - leftIndex) / 2;
			final T pivotValue = list.get(pivotIndex);
			
			// partition
			int equalsStartIndex = rightIndex;
			{
				// put pivot item on the right
				T tmp = list.get(rightIndex);
				list.set(rightIndex, pivotValue);
				list.set(pivotIndex, tmp);
			}
			int moveIndex = leftIndex;
			int largerStartIndex = leftIndex;
			while (moveIndex < equalsStartIndex) {
				
				final T moveValue = list.get(moveIndex);
				final int cmp = comparator.compare(moveValue, pivotValue);
				
				if (cmp == 0) {
					
					// equal to pivot,
					// move to the right
					{
						T tmp = list.get(equalsStartIndex-1);
						list.set(equalsStartIndex-1, list.get(moveIndex));
						list.set(moveIndex, tmp);
						equalsStartIndex--;
					}
					
				} else if (cmp < 0) {
					
					// less than pivot,
					// move to the left
					{
						T tmp = list.get(largerStartIndex);
						list.set(largerStartIndex, list.get(moveIndex));
						list.set(moveIndex, tmp);
						largerStartIndex++;
					}
					moveIndex++;

				} else if (cmp > 0) {
					
					moveIndex++;
				}
			}
			
			// move equal items in place
			for (int equalsIndex=equalsStartIndex; equalsIndex<=rightIndex; equalsIndex++) {
				
				// exchange with larger items
				{
					final int exchangeIndex = largerStartIndex + equalsIndex - equalsStartIndex;
					T tmp = list.get(exchangeIndex);
					list.set(exchangeIndex, list.get(equalsIndex));
					list.set(equalsIndex, tmp);
				}
			}
			
			// queue sublists
			final int newSmallerEndIndex = largerStartIndex - 1;
			final int newLargerStartIndex = largerStartIndex + (rightIndex - equalsStartIndex + 1);
			if (newSmallerEndIndex - leftIndex > 0) {
				queue.add(new int[] {leftIndex, newSmallerEndIndex});
			}
			if (rightIndex - newLargerStartIndex >  0) {
				queue.add(new int[] {newLargerStartIndex, rightIndex});
			}
		}
	}
	
	
}
