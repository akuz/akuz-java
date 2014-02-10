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
	
	public void update(HeapEntry<K, V> updatedEntry) {
		
		int index = updatedEntry.getIndex();
		
		while (true) {
			
			siftDown(_list.get(index));
			
			if (index > 0) {
				
				int parentIndex = getParentIndex(index);
				HeapEntry<K, V> parentEntry = _list.get(parentIndex);
				HeapEntry<K, V> childEntry = _list.get(index);
				
				if (parentEntry.getKey().compareTo(childEntry.getKey()) > 0) {
					
					index = parentIndex;
					continue;

				} else {
					break;
				}
			} else {
				break;
			}
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
	

	private void siftDown(HeapEntry<K, V> entry) {
		
		int index = entry.getIndex();
		while (true) {
			
			int leftChildIndex = getLeftChildIndex(index);
			if (leftChildIndex >= _list.size()) {
				
				// no left child,
				// so we reached
				// bottom of tree
				break;
			}
			
			int rightChildIndex = getRightChildIndex(index);
			if (rightChildIndex >= _list.size()) {
				
				// no right child, so we will
				// compare with left child only
				HeapEntry<K, V> leftChildEntry = _list.get(leftChildIndex);
				if (entry.getKey().compareTo(leftChildEntry.getKey()) > 0) {
					
					// exchange nodes
					_list.set(index, leftChildEntry);
					_list.set(leftChildIndex, entry);
					
					leftChildEntry.setIndex(index);
					entry.setIndex(leftChildIndex);
				}
				
				// finish, as this is 
				// the last level because
				/// there is no right child
				break;
			}
			
			// choose largest index
			int smallestIndex = index;
			{
				HeapEntry<K, V> smallestEntry = _list.get(smallestIndex);
				HeapEntry<K, V> leftChildEntry = _list.get(leftChildIndex);
				if (smallestEntry.getKey().compareTo(leftChildEntry.getKey()) > 0) {
					smallestIndex = leftChildIndex;
				}
			}
			{
				HeapEntry<K, V> smallestEntry = _list.get(smallestIndex);
				HeapEntry<K, V> rightChildEntry = _list.get(rightChildIndex);
				if (smallestEntry.getKey().compareTo(rightChildEntry.getKey()) > 0) {
					smallestIndex = rightChildIndex;
				}
			}
			
			// check if need to exchange
			if (index != smallestIndex) {
				
				// exchange nodes
				HeapEntry<K, V> smallestEntry = _list.get(smallestIndex);
				_list.set(index, smallestEntry);
				_list.set(smallestIndex, entry);
				
				smallestEntry.setIndex(index);
				entry.setIndex(smallestIndex);
				
				// go deeper
				index = smallestIndex;

			} else {
				
				// sifted
				break;
			}
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
