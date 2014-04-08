package me.akuz.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Index interface, allowing to arrange an arbitrary set of values 
 * into a list, and then access values by index or vice versa.
 * 
 */
public interface Index<T> {
	
	/**
	 * Ensures that the value is indexed.
	 * @param value
	 * @return
	 */
	public Integer ensure(T value);
	
	/**
	 * Ensures that all values from the collection are indexed.
	 * @param value
	 */
	public void ensureAll(Collection<T> value);
	
	/**
	 * Ensures that value is indexed, and returns previously cached
	 * value (if any) or the value just cached (argument).
	 * This is useful if you want to maintain pointers only 
	 * to one copy of "the same" (according to equals()) objects.
	 * @param value
	 * @return
	 */
	public T ensureGetCachedValue(T value);
	
	/**
	 * Current size of the index.
	 * @return
	 */
	public int size();
	
	/**
	 * Get the underlying list of added values.
	 * The position of the value in this list
	 * is its index.
	 * @return
	 */
	public List<T> getList();
	
	/**
	 * Get map of links from value to its index.
	 * @return
	 */
	public Map<T, Integer> getMap();
	
	/**
	 * Get index of a value (returns null, if value not cached).
	 * @param value
	 * @return
	 */
	public Integer getIndex(T value);
	
	/**
	 * Get value by index.
	 * @param index
	 * @return
	 */
	public T getValue(int index);
}
