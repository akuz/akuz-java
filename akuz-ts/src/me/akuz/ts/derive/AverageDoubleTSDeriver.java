package me.akuz.ts.derive;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.akuz.ts.TItem;
import me.akuz.ts.TFrame;
import me.akuz.ts.align.TSAlignIterator;

public final class AverageDoubleTSDeriver<T extends Comparable<T>> {

	public <K> TFrame<K, T> derive(final TFrame<K, T> inputFrame, final K outputKey) {
		
		Set<T> times = new HashSet<>();
		inputFrame.extractTimes(times);
		
		Set<K> keys = inputFrame.getMap().keySet();
		
		TSAlignIterator<K, T> iterator = new TSAlignIterator<>(inputFrame, times, keys);
		final TFrame<K, T> outputFrame = new TFrame<>();
		while (iterator.hasNext()) {
			
			Map<K, TItem<T>> currValues = iterator.next();
			
			double average = 0;
			for (Entry<K, TItem<T>> entry : currValues.entrySet()) {
				
				TItem<T> item = entry.getValue();
				average += item.getNumber().doubleValue();
			}
			
			average /= keys.size();
			
			outputFrame.add(outputKey, iterator.getCurrTime(), average);
		}
	
		return outputFrame;
	}

}
