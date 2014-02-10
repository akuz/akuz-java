package me.akuz.core.sort;

public final class LRUCacheEntry<K, V> {
	
	private final HeapEntry<Long, K> _heapEntry;
	private final V _value;
	
	public LRUCacheEntry(HeapEntry<Long, K> heapEntry, V value) {
		_heapEntry = heapEntry;
		_value = value;
	}
	
	public HeapEntry<Long, K> getHeapEntry() {
		return _heapEntry;
	}
	
	public K getKey() {
		return _heapEntry.getValue();
	}
	
	public V getValue() {
		return _value;
	}

}
