package me.akuz.core.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.akuz.core.ComparableComparator;
import me.akuz.core.EqualsUtils;
import me.akuz.core.SortOrder;
import me.akuz.core.sort.BinaryInsertSort;
import me.akuz.core.sort.HeapSort;
import me.akuz.core.sort.InsertSort;
import me.akuz.core.sort.MergeSort;
import me.akuz.core.sort.QuickSort;

import org.junit.Test;

public final class AllSortsTest {

	@Test
	public void test1_quality() {
		
		Random rnd = new Random(System.currentTimeMillis());
		final int EXPERIMENT_COUNT = 100;
		final int MAX_LIST_VALUE = 100;
		final int MAX_LIST_SIZE = 100;
		
		for (int experimentNumber=1; experimentNumber<=EXPERIMENT_COUNT; experimentNumber++) {
			
			// create control list
			final int listSize = rnd.nextInt(MAX_LIST_SIZE);
			final List<Integer> controlList = new ArrayList<>();
			for (int i=0; i<listSize; i++) {
				controlList.add(rnd.nextInt(MAX_LIST_VALUE));
			}
			
			// create lists to sort
			final List<Integer> binaryInsertSortList = new ArrayList<>(controlList);
			final List<Integer> heapSortList = new ArrayList<>(controlList);
			final List<Integer> insertSortList = new ArrayList<>(controlList);
			final List<Integer> mergeSortList = new ArrayList<>(controlList);
			final List<Integer> quickSortList = new ArrayList<>(controlList);
			
			// sample natural or unnatural order
			boolean isNaturalOrder = rnd.nextDouble() > 0.667;
			
			if (isNaturalOrder) {

				//System.out.println(this.getClass().getSimpleName() + " experiment " + experimentNumber + " - natural");

				// natural sort
				Collections.sort(controlList);
				QuickSort.sort(binaryInsertSortList);
				QuickSort.sort(heapSortList);
				QuickSort.sort(insertSortList);
				QuickSort.sort(mergeSortList);
				QuickSort.sort(quickSortList);
			
			} else {
				
				// sample sort order
				SortOrder sortOrder = rnd.nextDouble() > 0.5 ? SortOrder.Asc : SortOrder.Desc;
				
				//System.out.println(this.getClass().getSimpleName() + " experiment " + experimentNumber + " - unnatural - " + sortOrder);

				// create comparator
				ComparableComparator<Integer> comparator = new ComparableComparator<>(sortOrder);
				
				// unnatural sort
				Collections.sort(controlList, comparator);
				QuickSort.sort(binaryInsertSortList, comparator);
				QuickSort.sort(heapSortList, comparator);
				QuickSort.sort(insertSortList, comparator);
				QuickSort.sort(mergeSortList, comparator);
				QuickSort.sort(quickSortList, comparator);
			}
			
			// check results
			if (!EqualsUtils.equalsNullSafe(binaryInsertSortList, controlList)) {
				throw new IllegalStateException("binaryInsertSort is wrong");
			}
			if (!EqualsUtils.equalsNullSafe(heapSortList, controlList)) {
				throw new IllegalStateException("heapSort is wrong");
			}
			if (!EqualsUtils.equalsNullSafe(insertSortList, controlList)) {
				throw new IllegalStateException("insertSort is wrong");
			}
			if (!EqualsUtils.equalsNullSafe(mergeSortList, controlList)) {
				throw new IllegalStateException("mergeSort is wrong");
			}
			if (!EqualsUtils.equalsNullSafe(quickSortList, controlList)) {
				throw new IllegalStateException("quickSort is wrong");
			}
		}
	}
	
	@Test
	public void test2_performance() {
		
		final int SAMPLES = 10000;
		final int LIST_SIZE = 100;
		final int MAX_VALUE = 100;
		Random rnd = new Random(System.currentTimeMillis());
		
		long ms;
		ArrayList<Integer> list = new ArrayList<>();
		
		ms = System.currentTimeMillis();
		for (int s=0; s<SAMPLES; s++) {
			for (int i=0; i<LIST_SIZE; i++) {
				list.add(rnd.nextInt(MAX_VALUE));
			}
			Collections.sort(list);
			list.clear();
		}
		System.out.println("SystemSort speed: " + (System.currentTimeMillis() - ms) + " ms");
		
		ms = System.currentTimeMillis();
		for (int s=0; s<SAMPLES; s++) {
			for (int i=0; i<LIST_SIZE; i++) {
				list.add(rnd.nextInt(MAX_VALUE));
			}
			BinaryInsertSort.sort(list);
			list.clear();
		}
		System.out.println("BinaryInsertSort speed: " + (System.currentTimeMillis() - ms) + " ms");
		
		ms = System.currentTimeMillis();
		for (int s=0; s<SAMPLES; s++) {
			for (int i=0; i<LIST_SIZE; i++) {
				list.add(rnd.nextInt(MAX_VALUE));
			}
			HeapSort.sort(list);
			list.clear();
		}
		System.out.println("HeapSort speed: " + (System.currentTimeMillis() - ms) + " ms");
		
		ms = System.currentTimeMillis();
		for (int s=0; s<SAMPLES; s++) {
			for (int i=0; i<LIST_SIZE; i++) {
				list.add(rnd.nextInt(MAX_VALUE));
			}
			InsertSort.sort(list);
			list.clear();
		}
		System.out.println("InsertSort speed: " + (System.currentTimeMillis() - ms) + " ms");
		
		ms = System.currentTimeMillis();
		for (int s=0; s<SAMPLES; s++) {
			for (int i=0; i<LIST_SIZE; i++) {
				list.add(rnd.nextInt(MAX_VALUE));
			}
			MergeSort.sort(list);
			list.clear();
		}
		System.out.println("MergeSort speed: " + (System.currentTimeMillis() - ms) + " ms");
		
		ms = System.currentTimeMillis();
		for (int s=0; s<SAMPLES; s++) {
			for (int i=0; i<LIST_SIZE; i++) {
				list.add(rnd.nextInt(MAX_VALUE));
			}
			QuickSort.sort(list);
			list.clear();
		}
		System.out.println("QuickSort speed: " + (System.currentTimeMillis() - ms) + " ms");
	}
}
