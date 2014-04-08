package me.akuz.core;

public final class Out<T> {
	
	private T _value;
	
	public Out() {
		_value = null;
	}
	
	public Out(T defaultValue) {
		_value = defaultValue;
	}
	
	public T getValue() {
		return _value;
	}
	
	public void setValue(T value) {
		_value = value;
	}

	public boolean hasValue() {
		return _value != null;
	}
	
	@Override
	public String toString() {
		return _value != null ? _value.toString() : "";
	}
}
