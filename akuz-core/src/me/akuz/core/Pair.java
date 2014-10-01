package me.akuz.core;

public final class Pair<T1,T2> {
	
	private T1 _v1;
	private T2 _v2;
	
	public Pair(T1 v1, T2 v2) {
		_v1 = v1;
		_v2 = v2;
	}
	
	public T1 v1() {
		return _v1;
	}
	
	public T2 v2() {
		return _v2;
	}
	
	public void setV1(T1 v1) {
		_v1 = v1;
	}

	public void setV2(T2 v2) {
		_v2 = v2;
	}

	public String toString() {
		return "{" + _v1 + ", " + _v2 + "}";
	}
}
