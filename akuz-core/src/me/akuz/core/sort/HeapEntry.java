package me.akuz.core.sort;

public final class HeapEntry<K extends Comparable<K>, V> {

	private int _index;
	private K _key;
	private final V _value;
	
	public HeapEntry(int index, K key, V value) {
		_index = index;
		_key = key;
		_value = value;
	}
	
	public int getIndex() {
		return _index;
	}
	public void setIndex(int index) {
		_index = index;
	}
	
	public K getKey() {
		return _key;
	}
	public void setKey(K key) {
		_key = key;
	}
	
	public V getValue() {
		return _value;
	}
	
	@Override
	public String toString() {
		return String.format("{%s, %s}", _key, _value);
	}
}
