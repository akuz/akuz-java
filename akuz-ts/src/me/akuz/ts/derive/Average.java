package me.akuz.ts.derive;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.akuz.ts.TItem;
import me.akuz.ts.TFrame;
import me.akuz.ts.filters.TFrameStepper;

public final class Average {

	/**
	 * Calculate average of all sequences in a frame,
	 * and return the result sequence in a new frame.
	 */
	public static <K, T extends Comparable<T>> 
	TFrame<K, T> calc(
			final TFrame<K, T> frame,
			final K toKey) {
		
		return calc(
				frame, 
				toKey,
				false);
	}

	/**
	 * Calculate average of all sequences in a frame,
	 * and add the result sequence to the same frame,
	 * if inPlace is True, otherwise return new frame.
	 */
	public static <K, T extends Comparable<T>> 
	TFrame<K, T> calc(
			final TFrame<K, T> frame,
			final K toKey,
			final boolean inPlace) {
		
		return calc(
				frame,
				frame.getKeys(),
				toKey,
				inPlace);
	}

	/**
	 * Calculate average of specific sequences in a frame,
	 * and return the result sequence in a new frame.
	 */
	public static <K, T extends Comparable<T>> 
	TFrame<K, T> calc(
			final TFrame<K, T> frame,
			final List<K> keys,
			final K toKey) {
		
		return calc(
				frame,
				keys,
				toKey,
				false);
	}
	
	/**
	 * Calculate average of specific sequences in a frame,
	 * and add the result sequence to the same frame,
	 * if inPlace is True, otherwise return new frame.
	 */
	public static <K, T extends Comparable<T>> 
	TFrame<K, T> calc(
			final TFrame<K, T> frame,
			final List<K> keys,
			final K toKey,
			final boolean inPlace) {
		
		final Set<T> times = new HashSet<>();
		frame.extractTimes(times);
		
		final TFrame<K, T> toFrame;
		if (inPlace) {
			toFrame = frame;
		} else {
			toFrame = new TFrame<>();
		}
		
		final TFrameStepper<K, T> frameAligner = new TFrameStepper<>(frame, keys, times);
		while (frameAligner.hasNext()) {
			
			frameAligner.next();
			
			final Map<K, TItem<T>> currValues = frameAligner.getCurrItems();
			
			double average = 0;
			for (Entry<K, TItem<T>> entry : currValues.entrySet()) {
				
				TItem<T> item = entry.getValue();
				average += item.getNumber().doubleValue();
			}
			
			average /= keys.size();
			
			toFrame.add(toKey, frameAligner.getCurrTime(), average);
		}
	
		return toFrame;
	}

}
