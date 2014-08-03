package me.akuz.ml.signals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class SignalRollMulti<TKey> {
	
	private final double _periodInDays;
	private final Map<TKey, SignalRoll<TKey>> _rolls;
	
	public SignalRollMulti(double periodInDays) {
		_periodInDays = periodInDays;
		_rolls = new HashMap<TKey, SignalRoll<TKey>>();
	}
	
	public void add(TKey key, Date date, Double value) {
		
		SignalRoll<TKey> roll = _rolls.get(key);
		if (roll == null) {
			roll = new SignalRoll<TKey>(key);
			_rolls.put(key, roll);
		}
		
		roll.add(date, value);
	}
	
	public Map<TKey, Double> calculate(Date now) {
		
		Map<TKey, Double> result = new HashMap<TKey, Double>();
		
		for (SignalRoll<TKey> roll : _rolls.values()) {
			
			double signal = roll.calculate(now, _periodInDays);
			result.put(roll.getKey(), signal);
		}
		
		return result;
	}
}
