package me.akuz.core.math;

import java.util.HashSet;
import java.util.Set;

public final class SetUtils {

	public static final <T> Set<T> intersect(Set<T> set1, Set<T> set2) {
		if (set1.size() > set2.size()) {
			Set<T> temp = set2;
			set2 = set1;
			set1 = temp;
		}
		Set<T> intersection = new HashSet<T>();
		for (T i1 : set1) {
			if (set2.contains(i1)) {
				intersection.add(i1);
			}
		}
		return intersection;
	}
}
