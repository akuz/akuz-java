package me.akuz.ts.derive;

import java.util.Map;
import java.util.Map.Entry;

import me.akuz.ts.TSItem;
import me.akuz.ts.TSMap;
import me.akuz.ts.align.TSAlignIterator;

public final class AverageDoubleTSDeriver<T extends Comparable<T>> {

	public <K> TSMap<K, T> derive(final K outputKey, final TSMap<K, T> inputTSMap) {
		
		TSAlignIterator<K, T> iterator = new TSAlignIterator<>(inputTSMap.getMap(), inputTSMap.getTimes(), inputTSMap.getKeys());
		final TSMap<K, T> outputTSMap = new TSMap<>();
		while (iterator.hasNext()) {
			
			Map<K, TSItem<T>> currValues = iterator.next();
			
			double average = 0;
			for (Entry<K, TSItem<T>> entry : currValues.entrySet()) {
				
				TSItem<T> item = entry.getValue();
				average += item.getNumber().doubleValue();
			}
			
			average /= inputTSMap.getKeys().size();
			
			outputTSMap.add(outputKey, iterator.getCurrTime(), average);
		}
	
		return outputTSMap;
	}

}
