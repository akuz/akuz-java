package me.akuz.ml.signals;

import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import me.akuz.core.DateUtils;
import me.akuz.core.Pair;
import me.akuz.core.math.StatsUtils;

public final class SignalRoll<TKey> {
	
	private final TKey _key;
	private final Deque<Pair<Date, Double>> _dates; 
	
	public SignalRoll(TKey key) {
		_key = key;
		_dates = new LinkedList<Pair<Date,Double>>();
	}
	
	public TKey getKey() {
		return _key;
	}
	
	public void add(Date date, Double value) {
		_dates.add(new Pair<Date, Double>(date, value));
	}
	
	public double calculate(Date now, double periodInDays) {
		
		double result = 0;
		double std = periodInDays / 3.0;
		
		Iterator<Pair<Date, Double>> i = _dates.iterator();
		while (i.hasNext()) {
			
			Pair<Date, Double> pair = i.next();
			Date date = pair.v1();
			Double value = pair.v2();

			double daysBetween = DateUtils.daysBetween(date, now);
			
			if (daysBetween < 0) {
				
				// date is in the future, keep for later
				continue;
			}
			
			if (daysBetween > periodInDays) {
				
				// date is too old in the past, remove
				i.remove();
				continue;
			}
			
			double weight = StatsUtils.calcDistanceWeightGaussian(daysBetween, std);
			
			result += weight * value.doubleValue();
		}
		
		return result;
	}

}
