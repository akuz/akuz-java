package me.akuz.ts.derive;

import java.util.List;
import java.util.Map.Entry;

import me.akuz.ts.TSeq;
import me.akuz.ts.TItem;
import me.akuz.ts.TFrame;
import me.akuz.ts.TType;

public final class AccumDoubleTSDeriver<T extends Comparable<T>> {

	public TSeq<T> derive(TSeq<T> tsInput) {
		return derive(tsInput, 0.0);
	}

	public TSeq<T> derive(final TSeq<T> tsInput, final double initialValue) {
		
		TSeq<T> tsOutput = new TSeq<>(TType.DoubleType);
		double accumValue = initialValue;
		
		List<TItem<T>> items = tsInput.getItems();
		for (int i=0; i<items.size(); i++) {
			
			TItem<T> item = items.get(i);
			accumValue += item.getDouble().doubleValue();
			tsOutput.add(new TItem<>(item.getTime(), accumValue));
		}
		
		return tsOutput;
	}

	public <K> TFrame<K, T> derive(TFrame<K, T> inputTSMap) {
		return derive(inputTSMap, 0.0);
	}

	public <K> TFrame<K, T> derive(final TFrame<K, T> inputTSMap, final double initialValue) {
		
		final TFrame<K, T> outputTSMap = new TFrame<>();
		
		for (Entry<K, TSeq<T>> entry : inputTSMap.getMap().entrySet()) {
			
			final K key = entry.getKey();
			final TSeq<T> tsInput = entry.getValue();
			
			double accumValue = initialValue;
			List<TItem<T>> items = tsInput.getItems();

			for (int i=0; i<items.size(); i++) {
				
				TItem<T> item = items.get(i);
				accumValue += item.getNumber().doubleValue();
				outputTSMap.add(key, item.getTime(), accumValue);
			}
		}
		
		return outputTSMap;
	}

}
