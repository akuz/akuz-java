package me.akuz.ml.signals;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import me.akuz.core.DateUtils;
import me.akuz.core.Triple;
import me.akuz.core.math.Distribution;

public class MovingDist<D extends Distribution> {

	private final D _dist;
	private final Deque<Triple<Double, Double, Date>> _list;
	
	public MovingDist(D dist) {
		_dist = dist;
		_list = new LinkedList<Triple<Double, Double, Date>>();
	}
	
	public D getDist() {
		return _dist;
	}
	
	public void add(Double value, Double weight, Date time) {
		
		if (_list.size() > 0 && _list.getLast().v3().compareTo(time) > 0) {
			throw new IllegalStateException("Values should be added in chronological order");
		}
		
		_list.addLast(new Triple<Double, Double, Date>(value, weight, time));
		_dist.addObservation(value, weight);
	}
	
	public void removeBeforeAndReweight(Date tStart, Date tEnd) {
		
		double interval = DateUtils.daysBetween(tStart, tEnd);
		if (interval <= 0) {
			throw new InvalidParameterException("Parameter tStart should be < tEnd");
		}
		double variance = Math.pow(interval / 3.0, 2);
		
		_dist.reset();
		
		// remove old records
		while (_list.size() > 0) {
			Triple<Double, Double, Date> triple = _list.peek();
			if (triple.v3().compareTo(tStart) < 0) {
				triple = _list.removeFirst();
			} else {
				break;
			}
		}
		
		// update posterior
		Iterator<Triple<Double,Double,Date>> i = _list.iterator();
		while (i.hasNext()) {
			Triple<Double,Double,Date> triple = i.next();
			Date t = triple.v3();
			
			double tDist = DateUtils.daysBetween(t, tEnd);
			if (tDist < 0) {
				tDist = 0;
			}
			
			double tWeight = Math.exp(- tDist*tDist / 2.0 / variance);
			_dist.addObservation(triple.v1(), triple.v2() * tWeight);
		}
	}
}
