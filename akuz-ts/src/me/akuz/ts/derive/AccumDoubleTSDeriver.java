package me.akuz.ts.derive;

import java.util.List;
import java.util.Map.Entry;

import me.akuz.ts.TS;
import me.akuz.ts.TSItem;
import me.akuz.ts.TSMap;

public final class AccumDoubleTSDeriver<T extends Comparable<T>> {

	public TS<T> derive(TS<T> tsInput) {
		return derive(tsInput, 0.0);
	}

	public TS<T> derive(final TS<T> tsInput, final double initialValue) {
		
		TS<T> tsOutput = new TS<>();
		double accumValue = initialValue;
		
		List<TSItem<T>> items = tsInput.getItems();
		for (int i=0; i<items.size(); i++) {
			
			TSItem<T> item = items.get(i);
			accumValue += item.getDouble().doubleValue();
			tsOutput.add(new TSItem<>(item.getTime(), accumValue));
		}
		
		return tsOutput;
	}

	public <K> TSMap<K, T> derive(TSMap<K, T> inputTSMap) {
		return derive(inputTSMap, 0.0);
	}

	public <K> TSMap<K, T> derive(final TSMap<K, T> inputTSMap, final double initialValue) {
		
		final TSMap<K, T> outputTSMap = new TSMap<>();
		
		for (Entry<K, TS<T>> entry : inputTSMap.getMap().entrySet()) {
			
			final K key = entry.getKey();
			final TS<T> tsInput = entry.getValue();
			
			double accumValue = initialValue;
			List<TSItem<T>> items = tsInput.getItems();

			for (int i=0; i<items.size(); i++) {
				
				TSItem<T> item = items.get(i);
				accumValue += item.getNumber().doubleValue();
				outputTSMap.add(key, item.getTime(), accumValue);
			}
		}
		
		return outputTSMap;
	}

}
