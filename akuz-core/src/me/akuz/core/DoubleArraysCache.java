package me.akuz.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DoubleArraysCache {
	
	private final int _maxSizePerLength;
	private final Map<Integer, List<double[]>> _cacheByLength;
	private final Map<Integer, Integer> _cacheSizeByLength;
	
	public DoubleArraysCache(final int maxSizePerLength) {
		_maxSizePerLength = maxSizePerLength;
		_cacheByLength = new HashMap<>();
		_cacheSizeByLength = new HashMap<>();
	}
	
	public final double[] get(Integer length) {
		List<double[]> list = _cacheByLength.get(length);
		if (list != null && list.size() > 0) {
			return list.remove(list.size()-1);
		} else {
			Integer sizePerLength = _cacheSizeByLength.get(length);
			if (sizePerLength == null) {
				sizePerLength = 0;
			}
			sizePerLength += 1;
			if (sizePerLength <= _maxSizePerLength) {
				_cacheSizeByLength.put(length, sizePerLength);
				return new double[length];
			} else {
				throw new IllegalStateException("Max cache size for length " + length + " reached, please return some values first");
			}
		}
	}
	
	public final void ret(double[] arr) {
		List<double[]> list = _cacheByLength.get(arr.length);
		if (list == null) {
			list = new ArrayList<>();
			_cacheByLength.put(arr.length, list);
		}
		list.add(arr);
	}

}
