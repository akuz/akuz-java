package me.akuz.core.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.akuz.core.ComparableComparator;
import me.akuz.core.SortOrder;

/**
 * "Merge Sort" algorithm implementation.
 * 
 * Benefit: Average and worst-case performance of O(n*log(n)).
 *
 * Drawback: Uses O(n) additional memory.
 *
 */
public final class MergeSort {

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
		
		// working lists
		List<T> listFrom = list;
		List<T> listTo = new ArrayList<>(list);
		
		// merge with increasing width
		for (int width=1; width < list.size(); width = width * 2) {
			
			// merge all runs of the current width
			for (int i=0; i<listFrom.size(); i = i + 2 * width) {
				
				// runs bounds
				final int iStart = i;
				int i1 = iStart;
				final int iMid = Math.min(i + width, listFrom.size());
				int i2 = iMid;
				final int iEnd = Math.min(iMid + width, listFrom.size());
			
				// looping through merged index
				for (int j=iStart; j<iEnd; j++) {

					// if there are still items in run 1 &&
					// (there are no more items in run 2 ||
					//  item in run 1 is <= item in run 2)
					if (i1 < iMid && (i2 >= iEnd || comparator.compare(listFrom.get(i1), listFrom.get(i2)) <= 0)) {
						listTo.set(j, listFrom.get(i1));
						i1++;
					} else {
						listTo.set(j, listFrom.get(i2));
						i2++;
					}
				}
			}
			
			// exchange lists
			{
				List<T> tmp = listTo;
				listTo = listFrom;
				listFrom = tmp;
			}
		}
		
		// copy final, if needed
		if (listFrom != list) {
			for (int i=0; i<listFrom.size(); i++) {
				list.set(i, listFrom.get(i));
			}
		}
	}
	
}
