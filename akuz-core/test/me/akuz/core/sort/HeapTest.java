package me.akuz.core.sort;

import me.akuz.core.StringUtils;
import me.akuz.core.sort.Heap;
import me.akuz.core.sort.HeapEntry;

import org.junit.Test;

public class HeapTest {

	@Test
	public void test() {
		
		Heap<Integer, Object> heap = new Heap<>();
		
		heap.add(5, null);
		heap.add(4, null);
		HeapEntry<Integer, Object> thrEntry1 = heap.add(3, null);
		HeapEntry<Integer, Object> twoEntry1 = heap.add(2, null);
		
		System.out.println("Heap: " + StringUtils.collectionToString(heap.getList(), ", "));
		
		if (heap.size() != 4) {
			throw new IllegalStateException("Heap test error");
		}
		if (heap.getTop().getKey().equals(2) == false) {
			throw new IllegalStateException("Heap test error");
		}

		HeapEntry<Integer, Object> oneEntry1 = heap.add(1, null);
		HeapEntry<Integer, Object> zeroEntry = heap.add(0, null);
		
		System.out.println("Heap: " + StringUtils.collectionToString(heap.getList(), ", "));

		if (heap.size() != 6) {
			throw new IllegalStateException("Heap test error");
		}
		if (heap.getTop().getKey().equals(0) == false) {
			throw new IllegalStateException("Heap test error");
		}

		HeapEntry<Integer, Object> oneEntry2 = heap.add(1, null);
		HeapEntry<Integer, Object> twoEntry2 = heap.add(2, null);
		HeapEntry<Integer, Object> thrEntry2 = heap.add(3, null);
		heap.add(4, null);
		heap.add(5, null);
		
		System.out.println("Heap: " + StringUtils.collectionToString(heap.getList(), ", "));
		
		if (heap.size() != 11) {
			throw new IllegalStateException("Heap test error");
		}
		if (heap.getTop().getKey().equals(0) == false) {
			throw new IllegalStateException("Heap test error");
		}
		
		heap.remove(zeroEntry);
		
		System.out.println("Heap: " + StringUtils.collectionToString(heap.getList(), ", "));
		
		if (heap.size() != 10) {
			throw new IllegalStateException("Heap test error");
		}
		if (heap.getTop().getKey().equals(1) == false) {
			throw new IllegalStateException("Heap test error");
		}
		
		heap.remove(oneEntry1);
		heap.remove(oneEntry2);
		
		System.out.println("Heap: " + StringUtils.collectionToString(heap.getList(), ", "));
		
		if (heap.size() != 8) {
			throw new IllegalStateException("Heap test error");
		}
		if (heap.getTop().getKey().equals(2) == false) {
			throw new IllegalStateException("Heap test error");
		}
		
		heap.remove(twoEntry1);
		heap.remove(twoEntry2);
		
		System.out.println("Heap: " + StringUtils.collectionToString(heap.getList(), ", "));
		
		if (heap.size() != 6) {
			throw new IllegalStateException("Heap test error");
		}
		if (heap.getTop().getKey().equals(3) == false) {
			throw new IllegalStateException("Heap test error");
		}
		
		heap.remove(thrEntry1);
		heap.remove(thrEntry2);
		
		System.out.println("Heap: " + StringUtils.collectionToString(heap.getList(), ", "));
		
		if (heap.size() != 4) {
			throw new IllegalStateException("Heap test error");
		}
		if (heap.getTop().getKey().equals(4) == false) {
			throw new IllegalStateException("Heap test error");
		}
	}
}
