package me.akuz.core;

import java.util.Collection;
import java.util.List;

public final class CollectionUtils<T> {
	
	public static final CollectionUtils<Long>           Long = new CollectionUtils<>();
	public static final CollectionUtils<Integer>        Integer = new CollectionUtils<>();
	public static final CollectionUtils<String>         String = new CollectionUtils<>();
	public static final CollectionUtils<java.util.Date> Date = new CollectionUtils<>();
	
	public final boolean containsAllFromList(Collection<T> coll, List<T> list) {
		for (int i=0; i<list.size(); i++) {
			T item = list.get(i);
			if (coll.contains(item) == false) {
				return false;
			}
		}
		return true;
	}
	
	public final boolean containsAnyFromList(Collection<T> coll, List<T> list) {
		for (int i=0; i<list.size(); i++) {
			T item = list.get(i);
			if (coll.contains(item)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsAny(Collection<T> coll1, Collection<T> coll2) {
		
		for (T c2 : coll2) {
			if (coll1.contains(c2)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsAll(Collection<T> coll1, Collection<T> coll2) {
		
		return coll1.containsAll(coll2);
	}

	public boolean isExactMatch(List<T> list1, List<T> list2) {

		if (list1.size() != list2.size()) {
			return false;
		}
		for (int i=0; i<list1.size(); i++) {
			if (!list1.get(i).equals(list2.get(i))) {
				return false;
			}
		}
		return true;
	}

}
