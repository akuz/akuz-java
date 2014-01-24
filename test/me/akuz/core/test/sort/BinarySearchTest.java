package me.akuz.core.test.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.akuz.core.sort.BinarySearch;

import org.junit.Test;

public class BinarySearchTest {

	@Test
	public void test1_existing() {
		
		final int EXPERIMENT_COUNT = 1000;
		final int MAX_LIST_VALUE = 100;
		final int MAX_LIST_SIZE = 100;
		Random rnd = new Random(System.currentTimeMillis());

		for (int experimentNumber=1; experimentNumber<EXPERIMENT_COUNT; experimentNumber++) {
			
			//System.out.println(this.getClass().getSimpleName() + " experiment " + experimentNumber + " - existing");

			// create list to sort
			final int listSize = rnd.nextInt(MAX_LIST_SIZE);
			if (listSize == 0) {
				continue;
			}
			final List<Integer> list = new ArrayList<>();
			for (int i=0; i<listSize; i++) {
				list.add(rnd.nextInt(MAX_LIST_VALUE));
			}
			Collections.sort(list);
			
			int randomIndex = rnd.nextInt(list.size());
			int randomKey = list.get(randomIndex);
			
			int foundIndex = BinarySearch.find(list, randomKey);
			
			if (foundIndex < 0) {
				throw new IllegalStateException("Binary search couldn't find existing item");
			}
			
			if (!list.get(randomIndex).equals(list.get(foundIndex))) {
				throw new IllegalStateException("Binary search couldn't find correct index");
			}
			
		}
	}
	
	@Test
	public void test2_insertLeft() {
		
		final int EXPERIMENT_COUNT = 1000;
		final int MAX_LIST_VALUE = 100;
		final int MAX_LIST_SIZE = 100;
		Random rnd = new Random(System.currentTimeMillis());

		for (int experimentNumber=1; experimentNumber<EXPERIMENT_COUNT; experimentNumber++) {
			
			//System.out.println(this.getClass().getSimpleName() + " experiment " + experimentNumber + " - insert left");

			// create list to sort
			final int listSize = rnd.nextInt(MAX_LIST_SIZE);
			final List<Integer> list = new ArrayList<>();
			for (int i=0; i<listSize; i++) {
				list.add(rnd.nextInt(MAX_LIST_VALUE));
			}
			Collections.sort(list);
			
			int leftInsertKey = list.size() > 0 ? list.get(0) - 1 : 0;
			
			int foundIndex = BinarySearch.find(list, leftInsertKey);
			
			if (foundIndex >= 0) {
				throw new IllegalStateException("Binary search couldn't detect left insertion");
			}
			
			if (foundIndex != -1) {
				throw new IllegalStateException("Binary search couldn't find correct left insertion");
			}
			
		}
	}
	
	@Test
	public void test3_insertRight() {
		
		final int EXPERIMENT_COUNT = 1000;
		final int MAX_LIST_VALUE = 100;
		final int MAX_LIST_SIZE = 100;
		Random rnd = new Random(System.currentTimeMillis());

		for (int experimentNumber=1; experimentNumber<EXPERIMENT_COUNT; experimentNumber++) {
			
			//System.out.println(this.getClass().getSimpleName() + " experiment " + experimentNumber + " - insert right");

			// create list to sort
			final int listSize = rnd.nextInt(MAX_LIST_SIZE);
			final List<Integer> list = new ArrayList<>();
			for (int i=0; i<listSize; i++) {
				list.add(rnd.nextInt(MAX_LIST_VALUE));
			}
			Collections.sort(list);
			
			int rightInsertKey = list.size() > 0 ? list.get(list.size()-1) + 1 : 0;
			
			int foundIndex = BinarySearch.find(list, rightInsertKey);
			
			if (foundIndex >= 0) {
				throw new IllegalStateException("Binary search couldn't detect right insertion");
			}
			
			if (foundIndex != -1-list.size()) {
				throw new IllegalStateException("Binary search couldn't find correct right insertion");
			}
			
		}
	}
}
