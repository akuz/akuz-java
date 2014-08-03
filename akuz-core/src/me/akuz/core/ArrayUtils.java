package me.akuz.core;

import java.util.Arrays;
import java.util.List;

public final class ArrayUtils {

	public final static int[] integerListToArray(List<Integer> list) {
		int[] arr = new int[list.size()];
		for (int i=0; i<list.size(); i++) {
			arr[i] = list.get(i);
		}
		return arr;
	}

	public final static double[] doubleListToArray(List<Double> list) {
		double[] arr = new double[list.size()];
		for (int i=0; i<list.size(); i++) {
			arr[i] = list.get(i);
		}
		return arr;
	}

	public final static double[] initArray1D(int dim1) {
		return initArray1D(dim1, 0);
	}

	public final static double[] initArray1D(int dim1, double fill) {
		double[] arr = new double[dim1];
		if (fill != 0) {
			Arrays.fill(arr, fill);
		}
		return arr;
	}
	
	public final static double[][] initArray2D(int dim1, int dim2) {
		return initArray2D(dim1, dim2, 0);
	}

	public final static double[][] initArray2D(int dim1, int dim2, double fill) {
		double[][] arr = new double[dim1][];
		for (int i=0; i<arr.length; i++) {
			double[] col = new double[dim2];
			if (fill != 0) {
				Arrays.fill(col, fill);
			}
			arr[i] = col;
		}
		return arr;
	}

	public final static void fillArray2D(double[][] arr, double value) {
		for (int i=0; i<arr.length; i++) {
			double[] col = arr[i];
			Arrays.fill(col, value);
		}
	}

	public static boolean equals(byte[] arr1, byte[] arr2) {
		if (arr1 == arr2) {
			return true;
		}
		if (arr1 == null || arr2 == null) {
			return false;
		}
		if (arr1.length != arr2.length) {
			return false;
		}
		for (int i=0; i<arr1.length; i++) {
			if (arr1[i] != arr2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static final void addToFirst(double[] arr1, double[] arr2) {
		if (arr1.length != arr2.length) {
			throw new IllegalArgumentException("Arrays must have the same length");
		}
		for (int i=0; i<arr1.length; i++) {
			arr1[i] += arr2[i];
		}
	}
	
}
