package me.akuz.qf.data;

import java.util.Map;

/**
 * Abstract time series map.
 *
 * @param <K> - Key type.
 * @param <T> - Time type.
 */
public abstract class TSMap<K, T extends Comparable<T>> {

	public abstract Map<K, TS<T>> getMap();
}
