package me.akuz.core.math;

import java.util.HashMap;
import java.util.Map;

public final class SpVector {
	
	private final Map<Integer, Double> _data;
	
	public SpVector() {
		_data = new HashMap<>();
	}
	
	public final void add(Integer i, Double value) {
		Double currValue = _data.get(i);
		if (currValue != null) {
			_data.put(i, currValue + value);
		} else {
			_data.put(i, value);
		}
	}
	
	public final void set(Integer i, Double value) {
		_data.put(i, value);
	}
	
	public final Double get(Integer i) {
		return _data.get(i);
	}
	
	public final Double remove(Integer i) {
		return _data.remove(i);
	}
	
	public final void clear() {
		_data.clear();
	}

}
