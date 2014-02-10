package me.akuz.core.sort;

import java.util.HashMap;
import java.util.Map;

public final class LRUCache<K, V> {
	
	private long _counter;
	private final int _maxSize;
	private final Heap<Long, K> _heap;
	private final Map<K, LRUCacheEntry<K, V>> _map;
	
	public LRUCache(int maxSize) {
		if (maxSize < 1) {
			throw new IllegalArgumentException("maxSize must be positive");
		}
		_maxSize = maxSize;
		_heap = new Heap<>();
		_map = new HashMap<>();
	}
	
	public V get(K key) {
		
		// check in cache
		LRUCacheEntry<K, V> entry = _map.get(key);
		if (entry == null) {
			return null;
		}
		
		// update heap entry and resort O(log(n))
		entry.getHeapEntry().setKey(_counter++);
		_heap.update(entry.getHeapEntry());

		// return the value
		return entry.getValue();
	}
	
	public void add(K key, V value) {
		
		LRUCacheEntry<K, V> entry = _map.get(key);
		if (entry != null) {
			
			entry.getHeapEntry().setKey(_counter++);
			_heap.update(entry.getHeapEntry());

		} else {
		
			if (_map.containsKey(key)) {
				throw new IllegalStateException("Key already present in cache");
			}
			
			// add new heap entry
			HeapEntry<Long, K> heapEntry = _heap.add(_counter++, key);
			entry = new LRUCacheEntry<>(heapEntry, value);
			_map.put(key, entry);
			
			// clean up heap
			while (_heap.size() > _maxSize) {
				HeapEntry<Long, K> top = _heap.getTop();
				_map.remove(top.getValue());
				_heap.remove(top);
			}
		}
	}
	
	public Heap<Long, K> getHeap() {
		return _heap;
	}

}
