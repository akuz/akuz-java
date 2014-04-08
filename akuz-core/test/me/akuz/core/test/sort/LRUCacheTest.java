package me.akuz.core.test.sort;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.StringUtils;
import me.akuz.core.sort.LRUCache;

import org.junit.Test;

public class LRUCacheTest {

	@Test
	public void test() {
		
		Random rnd = ThreadLocalRandom.current();
		
		Set<Integer> set = new HashSet<>();
		while (set.size() < 50) {
			set.add(rnd.nextInt(100));
		}
		
		LRUCache<Integer, Object> cache = new LRUCache<>(10);
		Queue<Integer> lastKeys = new LinkedList<>();
		for (Integer key : set) {
			
			lastKeys.add(key);
			if (lastKeys.size() > 10) {
				lastKeys.poll();
			}
			
			cache.add(key, new Object());
		}
		
		Set<Integer> lastSet = new HashSet<>(lastKeys);
		System.out.println("Last set (" + lastSet.size() + "): " + StringUtils.collectionToString(lastSet, ", "));
		System.out.println("Heap (" + cache.getHeap().size() + "): " + StringUtils.collectionToString(cache.getHeap().getList(), ", "));
		for (Integer key : set) {
			
			if (lastSet.contains(key)) {
				if (cache.get(key) == null) {
					throw new IllegalStateException("Cache does not contain needed element: " + key);
				}
			} else if (cache.get(key) != null) {
				throw new IllegalStateException("Cache contains an element it should not: " + key);
			}
		}
	}
}
