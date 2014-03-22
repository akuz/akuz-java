package me.akuz.ts;

import java.util.Map;
import java.util.Set;

/**
 * Abstract time series map.
 *
 * @param <K> - Key type.
 * @param <T> - Time type.
 */
public abstract class TSMap<K, T extends Comparable<T>> {
	
	public abstract Set<K> getKeys();
	
	public abstract Set<T> getTimes();

	public abstract Map<K, TS<T>> getMap();

}
