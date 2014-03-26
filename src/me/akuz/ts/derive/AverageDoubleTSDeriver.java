package me.akuz.ts.derive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.akuz.ts.TSItem;
import me.akuz.ts.TSMap;
import me.akuz.ts.align.TSAlignIterator;

public final class AverageDoubleTSDeriver<T extends Comparable<T>> {

	public <K> TSMap<K, T> derive(final K outputKey, final TSMap<K, T> inputTSMap) {
		
		List<T> times = new ArrayList<>(inputTSMap.getTimes());
		if (times.size() > 1) {
			Collections.sort(times);
		}

		final TSMap<K, T> outputTSMap = new TSMap<>();

		TSAlignIterator<K, T> iterator = new TSAlignIterator<>(inputTSMap.getMap(), times, inputTSMap.getKeys());
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
