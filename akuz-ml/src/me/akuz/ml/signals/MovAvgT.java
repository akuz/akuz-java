package me.akuz.ml.signals;

import java.util.Deque;
import java.util.LinkedList;

import me.akuz.core.Pair;


public final class MovAvgT<T extends Comparable<T>> {
	
	private final Deque<Pair<Double, T>> _list;
	private double _avg;
	
	public MovAvgT() {
		_list = new LinkedList<Pair<Double,T>>();
		_avg = 0;
	}
	
	public double getValue() {
		return _avg;
	}
	
	public void add(Double value, T time) {
		
		if (_list.size() > 0 && _list.getLast().v2().compareTo(time) > 0) {
			throw new IllegalStateException("Values should be added in chronological order");
		}
		
		_list.addLast(new Pair<Double, T>(value, time));
		
		_avg = _avg / _list.size() * (_list.size() - 1) + value / _list.size();
	}
	
	public void removeBefore(T time) {
		while (_list.size() > 0) {
			
			Pair<Double, T> pair = _list.peek();
			if (pair.v2().compareTo(time) < 0) {
				
				pair = _list.removeFirst();
				
				if (_list.size() == 0) {
					_avg = 0;
				} else {
					_avg = _avg / _list.size() * (_list.size() + 1) - pair.v1() / _list.size();
				}

			} else {
				break;
			}
		}
	}

}
