package me.akuz.core.sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Heap data structure based on the comparable key.
 * 
 */
public final class Heap<K extends Comparable<K>, V> {
	
	private final List<HeapEntry<K, V>> _list;
	
	public Heap() {
		_list = new ArrayList<>();
	}
	
	public HeapEntry<K, V> add(K key, V value) {
		HeapEntry<K, V> entry = new HeapEntry<K, V>(_list.size(), key, value);
		_list.add(entry);
		update(entry);
		return entry;
	}
	
	public void update(HeapEntry<K, V> entry) {
		
		while (true) {
			
			// compare with parent
			if (entry.getIndex() > 0) {
				
				int parentIndex = getParentIndex(entry.getIndex());
				HeapEntry<K, V> parentEntry = _list.get(parentIndex);
				int cmp = parentEntry.getKey().compareTo(entry.getKey());
				if (cmp > 0) {
					
					_list.set(parentEntry.getIndex(), entry);
					_list.set(entry.getIndex(), parentEntry);
					
					parentEntry.setIndex(entry.getIndex());
					entry.setIndex(parentIndex);
					continue;
				}
			}
			
			// compare with left child
			int leftChildIndex = getLeftChildIndex(entry.getIndex());
			if (leftChildIndex < _list.size()) {
				
				HeapEntry<K, V> leftChildEntry = _list.get(leftChildIndex);
				int cmp = entry.getKey().compareTo(leftChildEntry.getKey());
				if (cmp > 0) {
					
					_list.set(leftChildIndex, entry);
					_list.set(entry.getIndex(), leftChildEntry);
					
					leftChildEntry.setIndex(entry.getIndex());
					entry.setIndex(leftChildIndex);
					continue;
				}
			}
			
			// compare with right child
			int rightChildIndex = getRightChildIndex(entry.getIndex());
			if (rightChildIndex < _list.size()) {
				
				HeapEntry<K, V> rightChildEntry = _list.get(rightChildIndex);
				int cmp = entry.getKey().compareTo(rightChildEntry.getKey());
				if (cmp > 0) {
					
					_list.set(rightChildIndex, entry);
					_list.set(entry.getIndex(), rightChildEntry);
					
					rightChildEntry.setIndex(entry.getIndex());
					entry.setIndex(rightChildIndex);
					continue;
				}
			}
			
			// no changes
			break;
		}
	}
	
	public void remove(HeapEntry<K, V> entry) {
		
		if (_list.size() == 0) {
			throw new IllegalStateException("Heap is empty");
		}
		int newSize = _list.size() - 1;
		int replaceIndex = entry.getIndex();
		entry.setIndex(-1);
		if (replaceIndex == newSize) {
			_list.remove(newSize);
		} else {
			HeapEntry<K, V> lastEntry = _list.get(newSize);
			_list.set(replaceIndex, lastEntry);
			lastEntry.setIndex(replaceIndex);
			_list.remove(newSize);
			update(lastEntry);
		}
	}
	
	public int size() {
		return _list.size();
	}
	
	public HeapEntry<K, V> getTop() {
		if (_list.size() <= 0) {
			throw new IllegalStateException("Heap is empty");
		}
		return _list.get(0);
	}
	
	private static final int getParentIndex(int index) {
		// integer division
		return (index -1 ) / 2;
	}
	
	private static final int getLeftChildIndex(int index) {
		return index * 2 + 1; 
	}
	
	private static final int getRightChildIndex(int index) {
		return index * 2 + 2;
	}

	public List<HeapEntry<K, V>> getList() {
		return _list;
	}
}
