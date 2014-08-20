package me.akuz.ts.io.custom;

public final class Quote {
	
	private final QuoteField _field;
	private final double _value;
	
	public Quote(
			final QuoteField field,
			final double value) {
		
		_field = field;
		_value = value;
	}
	
	public QuoteField getField() {
		return _field;
	}
	
	public double getValue() {
		return _value;
	}
	
	@Override
	public String toString() {
		return String.format("%s:%s", _field, _value);
	}

}
