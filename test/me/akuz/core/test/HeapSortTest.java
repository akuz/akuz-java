package me.akuz.core.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.akuz.core.ComparableComparator;
import me.akuz.core.EqualsUtils;
import me.akuz.core.HeapSort;
import me.akuz.core.SortOrder;
import me.akuz.core.StringUtils;

import org.junit.Test;

public final class HeapSortTest {

	@Test
	public void testHeapSort() {
		
		Random rnd = new Random(System.currentTimeMillis());
		final int EXPERIMENT_COUNT = 100;
		final int MAX_LIST_VALUE = 100;
		final int MAX_LIST_SIZE = 100;
		
		for (int experimentNumber=1; experimentNumber<=EXPERIMENT_COUNT; experimentNumber++) {
			
			// create list to sort
			final int listSize = rnd.nextInt(MAX_LIST_SIZE);
			final List<Integer> list1 = new ArrayList<>();
			for (int i=0; i<listSize; i++) {
				list1.add(rnd.nextInt(MAX_LIST_VALUE));
			}
			
			// create control list
			final List<Integer> list2 = new ArrayList<>(list1);
			
			// sample natural or unnatural order
			boolean isNaturalOrder = rnd.nextDouble() > 0.667;
			
			if (isNaturalOrder) {

				System.out.println(this.getClass().getSimpleName() + " experiment " + experimentNumber + " - natural");

				// natural sort
				HeapSort.sort(list1);
				Collections.sort(list2);
			
			} else {
				
				// sample sort order
				SortOrder sortOrder = rnd.nextDouble() > 0.5 ? SortOrder.Asc : SortOrder.Desc;
				
				System.out.println(this.getClass().getSimpleName() + " experiment " + experimentNumber + " - unnatural - " + sortOrder);

				// create comparator
				ComparableComparator<Integer> comparator = new ComparableComparator<>(sortOrder);
				
				// unnatural sort
				HeapSort.sort(list1, comparator);
				Collections.sort(list2, comparator);
			}
			
			// check results
			if (!EqualsUtils.equalsNullSafe(list1, list2)) {
				
				System.out.println("Sorted lists are not the same:" 
						+ "\n 1) " + StringUtils.collectionToString(list1, ",")
						+ "\n 2) " + StringUtils.collectionToString(list2, ","));
				
				throw new IllegalStateException("Sorted lists are not the same");
			}
		}
	}
}
