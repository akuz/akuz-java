package me.akuz.ts.derive;

import java.util.List;
import java.util.Map;

import me.akuz.ts.Seq;
import me.akuz.ts.TItem;
import me.akuz.ts.Frame;

/**
 * Cumsum for sequences and frames.
 *
 */
public final class Cumsum {

	/**
	 * Calculate cumsum for a sequence.
	 */
	public static <T extends Comparable<T>> 
	Seq<T> calc(final Seq<T> seq) {
		
		return calc(seq, 0.0);
	}

	/**
	 * Calculate cumsum for a sequence, 
	 * starting from the initialValue.
	 */
	public static <T extends Comparable<T>>
	Seq<T> calc(final Seq<T> seq, final double initialValue) {
		
		if (Double.isNaN(initialValue)) {
			throw new IllegalArgumentException("Initial value cannot be NAN");
		}
		
		final Seq<T> toSeq = new Seq<>();
		double accumValue = initialValue;
		
		final List<TItem<T>> items = seq.getItems();
		for (int i=0; i<items.size(); i++) {
			
			final TItem<T> item = items.get(i);
			final double value = item.getDouble().doubleValue();
			if (!Double.isNaN(value)) {
				accumValue += value;
				toSeq.add(new TItem<>(item.getTime(), accumValue));
			}
		}
		
		return toSeq;
	}

	/**
	 * Calculate cumsum for all sequences in a frame,
	 * returning a new frame with cumsum sequences having the
	 * original keys they had in the provided frame.
	 * @param frame
	 * @param keyMap
	 * @return
	 */
	public static <K, T extends Comparable<T>> 
	Frame<K, T> calc(final Frame<K, T> frame) {
		
		return calc(frame, null, false, 0.0);
	}

	/**
	 * Calculate cumsum for all sequences in a frame,
	 * starting from the provided initialValue,
	 * returning a new frame with cumsum sequences having the
	 * original keys they had in the provided frame.
	 * @param frame
	 * @param keyMap
	 * @return
	 */
	public static <K, T extends Comparable<T>> 
	Frame<K, T> calc(final Frame<K, T> frame, final double initialValue) {
		
		return calc(frame, null, false, initialValue);
	}

	/**
	 * Calculate cumsum for specific sequences in a frame,
	 * and either return a new frame, or add sequences inPlace.
	 * The keyMap must provide new keys for new sequences.
	 * @param frame
	 * @param keyMap
	 * @return
	 */
	public static <K, T extends Comparable<T>> 
	Frame<K, T> calc(final Frame<K, T> frame, final Map<K, K> keyMap, final boolean inPlace) {
		
		return calc(frame, keyMap, inPlace, 0.0);
	}

	/**
	 * Calculate cumsum for specific sequences in a frame,
	 * starting from the provided initialValue,
	 * and either return a new frame, or add sequences inPlace.
	 * The keyMap must provide new keys for new sequences.
	 * Null keyMap means to make cumsum for all keys,
	 * but in this case you can't use inPlace.
	 * @param frame
	 * @param keyMap
	 * @return
	 */
	public static <K, T extends Comparable<T>> 
	Frame<K, T> calc(final Frame<K, T> frame, final Map<K, K> keyMap, final boolean inPlace, final double initialValue) {
		
		if (inPlace && keyMap == null) {
			throw new IllegalArgumentException(
					"Cannot accept keyMap = null and inPlace = True, " +
					"since this would lead to adding new sequences with " +
					"the same keys as already exist in the frame");
		}
		
		final Frame<K, T> toFrame;
		if (inPlace && keyMap != null) {
			toFrame = frame;
		} else {
			toFrame = new Frame<>();
		}
		
		final List<K> keys = frame.getKeys();
		for (int i=0; i<keys.size(); i++) {
			
			final K key = keys.get(i);
			final K toKey;
			if (keyMap != null) {
				toKey = keyMap.get(key);
			} else {
				toKey = key;
			}
			if (toKey == null) {
				continue;
			}
			
			final Seq<T> seq = frame.getSeq(key);
			final Seq<T> toSeq = calc(seq, initialValue);
			toFrame.addSeq(toKey, toSeq);
		}
		
		return toFrame;
	}

}
