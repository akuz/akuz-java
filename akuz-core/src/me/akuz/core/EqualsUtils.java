package me.akuz.core;

import java.util.List;

public final class EqualsUtils {

	public static boolean equalsNullSafe(Object a0, Object a1) {
		if (a0 == a1) {
			return true;
		}
		if (a0 == null || 
			a1 == null) {
			return false;
		}
		return a0.equals(a1);
	}

	public static final <T> boolean equalsNullSafe(List<T> list1, List<T> list2) {
		
		if (list1 == list2) {
			return true;
		}
		if (list1 == null) {
			return list2 == null;
		}
		if (list2 == null) {
			return list1 == null;
		}
		if (list1.size() != list2.size()) {
			return false;
		}
		for (int i=0; i<list1.size(); i++) {
			if (!equalsNullSafe(list1.get(i), list2.get(i))) {
				return false;
			}
		}
		return true;
	}
}
