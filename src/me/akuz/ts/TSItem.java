package me.akuz.ts;

/**
 * Time series item.
 * 
 * Note: Only time type T is a generic parameter,
 * This is necessary in order to be able to create
 * tables (TSMap) containing time series (TS) with 
 * the same time type, but heterogeneous value types.
 * 
 * @param <T> - Time type.
 * 
 */
public final class TSItem<T extends Comparable<T>> implements Comparable<TSItem<T>> {
	
	private final T _time;
	private final Object _value;
	
	public TSItem(T time, Object value) {
		_time = time;
		_value = value;
	}
	
	public T getTime() {
		return _time;
	}
	
	public Boolean getBoolean() {
		return (Boolean)_value;
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
	public Object getObject() {
		return _value;
	}

	@Override
	public int compareTo(TSItem<T> o) {
		return _time.compareTo(o.getTime());
	}
}
