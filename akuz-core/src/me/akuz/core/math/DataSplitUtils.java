package me.akuz.core.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.akuz.core.Pair;
import me.akuz.core.ArrayUtils;

public final class DataSplitUtils {

	public static final Pair<int[], int[]> splitInOrder(int size, double pctFirst) {
		
		int lastTrainIdx = (int)Math.floor(size * pctFirst);
		
		List<Integer> list1 = new ArrayList<Integer>();
		List<Integer> list2 = new ArrayList<Integer>();
		
		for (int i=0; i<size; i++) {
			if (i <= lastTrainIdx) {
				list1.add(i);
			} else {
				list2.add(i);
			}
		}
		
		int[] arr1 = ArrayUtils.integerListToArray(list1);
		int[] arr2 = ArrayUtils.integerListToArray(list2);
		
		return new Pair<int[], int[]>(arr1, arr2);
	}

	public static final Pair<int[], int[]> splitAtRandom(int size, double pctFirst, Random rnd) {
		
		List<Integer> list1 = new ArrayList<Integer>();
		List<Integer> list2 = new ArrayList<Integer>();
		
		for (int i=0; i<size; i++) {
			
			if (rnd.nextDouble() <= pctFirst) {
				list1.add(i);
			} else {
				list2.add(i);
			}
		}
		
		int[] arr1 = ArrayUtils.integerListToArray(list1);
		int[] arr2 = ArrayUtils.integerListToArray(list2);
		
		return new Pair<int[], int[]>(arr1, arr2);
	}
}
