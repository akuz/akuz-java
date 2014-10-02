package me.akuz.core;

import java.util.Collection;

public final class PairUtils {
	
	public static final <T1 extends Comparable<T1>, T2> 
	T1 max1(final Collection<Pair<T1, T2>> collection) {
		
		T1 res = null;
		for (final Pair<T1, T2> pair : collection) {
			if (res == null) {
				res = pair.v1();
			} else {
				if (pair.v1() != null) {
					if (res.compareTo(pair.v1()) < 0) {
						res = pair.v1();
					}
				}
			}
		}
		return res;
	}
	
	public static final <T1 extends Comparable<T1>, T2> 
	T1 min1(final Collection<Pair<T1, T2>> collection) {
		
		T1 res = null;
		for (final Pair<T1, T2> pair : collection) {
			if (res == null) {
				res = pair.v1();
			} else {
				if (pair.v1() != null) {
					if (res.compareTo(pair.v1()) > 0) {
						res = pair.v1();
					}
				}
			}
		}
		return res;
	}
	
	public static final <T1, T2 extends Comparable<T2>> 
	T2 max2(final Collection<Pair<T1, T2>> collection) {
		
		T2 res = null;
		for (final Pair<T1, T2> pair : collection) {
			if (res == null) {
				res = pair.v2();
			} else {
				if (pair.v2() != null) {
					if (res.compareTo(pair.v2()) < 0) {
						res = pair.v2();
					}
				}
			}
		}
		return res;
	}
	
	public static final <T1, T2 extends Comparable<T2>> 
	T2 min2(final Collection<Pair<T1, T2>> collection) {
		
		T2 res = null;
		for (final Pair<T1, T2> pair : collection) {
			if (res == null) {
				res = pair.v2();
			} else {
				if (pair.v2() != null) {
					if (res.compareTo(pair.v2()) > 0) {
						res = pair.v2();
					}
				}
			}
		}
		return res;
	}

}
