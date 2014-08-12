package me.akuz.ts.derive;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.akuz.core.Out;
import me.akuz.ts.Frame;
import me.akuz.ts.FrameIterator;
import me.akuz.ts.TItem;

public final class Average {

	/**
	 * Calculate average of all sequences in a frame,
	 * and return the result sequence in a new frame.
	 */
	public static <K, T extends Comparable<T>> 
	Frame<K, T> calc(
			final Frame<K, T> frame,
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
	Frame<K, T> calc(
			final Frame<K, T> frame,
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
	Frame<K, T> calc(
			final Frame<K, T> frame,
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
	Frame<K, T> calc(
			final Frame<K, T> frame,
			final List<K> keys,
			final K toKey,
			final boolean inPlace) {
		
		final Frame<K, T> toFrame;
		if (inPlace) {
			toFrame = frame;
		} else {
			toFrame = new Frame<>();
		}
		
		final FrameIterator<K, T> frameIterator = new FrameIterator<K, T>(frame, keys);
		final Out<T> nextTime = new Out<>();
		while (frameIterator.getNextTime(nextTime)) {
			
			frameIterator.moveToTime(nextTime.getValue());
			
			final Map<K, TItem<T>> currValues = frameIterator.getCurrItems();
			
			double average = 0;
			for (Entry<K, TItem<T>> entry : currValues.entrySet()) {
				
				TItem<T> item = entry.getValue();
				average += item.getNumber().doubleValue();
			}
			
			average /= keys.size();
			
			toFrame.add(toKey, frameIterator.getCurrTime(), average);
		}
	
		return toFrame;
	}

}
