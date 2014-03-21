package me.akuz.ts;

/**
 * Time series entry.
 * 
 * Not making value type generic, in order to be able to have heterogeneous collections.
 *
 * @param <T> - Time type, must be comparable to each other.
 * 
 */
public final class TSEntry<T extends Comparable<T>> implements Comparable<TSEntry<T>> {
	
	private final T _time;
	private final Object _value;
	
	public TSEntry(T time, Object value) {
		_time = time;
		_value = value;
	}
	
	public T getTime() {
		return _time;
	}
	
	public Number getNumber() {
		return (Number)_value;
	}
	public Double getDouble() {
		return (Double)_value;
	}
	public Integer getInteger() {
		return (Integer)_value;
	}
	public Integer getLong() {
		return (Integer)_value;
	}
	public String getString() {
		return (String)_value;
	}

	@Override
	public int compareTo(TSEntry<T> o) {
		return _time.compareTo(o.getTime());
	}
}
