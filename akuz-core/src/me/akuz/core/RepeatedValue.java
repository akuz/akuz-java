package me.akuz.core;

import java.util.List;

public final class RepeatedValue<T> {
	
	private final int _count;
	private final T _value;
	
	public RepeatedValue(int count, T value) {
		_count = count;
		_value = value;
	}
	
	public int getCount() {
		return _count;
	}
	
	public T getValue() {
		return _value;
	}
	
	public final static int[] expandInteger(List<RepeatedValue<Integer>> list) {
		
		int count = 0;
		for (int j=0; j<list.size(); j++) {
			count += list.get(j).getCount();
		}

		int[] result = new int[count];
		int i = -1;
		for (int j=0; j<list.size(); j++) {
			RepeatedValue<Integer> multi = list.get(j);
			for (int s=0; s<multi.getCount(); s++) {
				i += 1;
				result[i] = multi.getValue();
			}
		}
		return result;
	}
	
	public final static double[] expandDouble(List<RepeatedValue<Double>> list) {
		
		int count = 0;
		for (int j=0; j<list.size(); j++) {
			count += list.get(j).getCount();
		}

		double[] result = new double[count];
		int i = -1;
		for (int j=0; j<list.size(); j++) {
			RepeatedValue<Double> multi = list.get(j);
			for (int s=0; s<multi.getCount(); s++) {
				i += 1;
				result[i] = multi.getValue();
			}
		}
		return result;
	}
}
