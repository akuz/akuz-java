package me.akuz.ts.io.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Quote containing one or more fields with a value.
 *
 */
public final class Quote {
	
	private final QuoteField[] _fields;
	private final double[] _values;
	
	/**
	 * Quote builder for optimizing the data use.
	 *
	 */
	public final static class Builder {
		
		private final Map<QuoteField, Double> _values;
		
		private Builder() {
			_values = new HashMap<>();
		}
		
		/**
		 * Set quote field value.
		 * 
		 */
		public Builder set(final QuoteField field, final double value) {
			_values.put(field, value);
			return this;
		}
		
		/**
		 * Finish building a quote.
		 * 
		 */
		public Quote create() {
			if (_values.size() == 0) {
				throw new IllegalStateException("Quote built so far doesn't have any values");
			}
			final QuoteField[] fields = new QuoteField[_values.size()];
			final double[] values = new double[_values.size()];
			int i = 0;
			for (Entry<QuoteField, Double> entry : _values.entrySet()) {
				fields[i] = entry.getKey();
				values[i] = entry.getValue();
				i++;
			}
			return new Quote(fields, values);
		}
	}
	
	/**
	 * Start building a quote.
	 * 
	 */
	public static final Builder build() {
		return new Builder();
	}
	
	private Quote(
			final QuoteField[] fields,
			final double[] values) {
		
		_fields = fields;
		_values = values;
	}
	
	/**
	 * Get field value, throw exception if missing.
	 * 
	 */
	public double get(final QuoteField field) { 
		for (int i=0; i<_fields.length; i++) {
			if (field.equals(_fields[i])) {
				return _values[i];
			}
		}
		throw new IllegalStateException("Quote has no field: " + field);
	}
	
	/**
	 * Get field value, return default if missing.
	 * 
	 */
	public double get(final QuoteField field, final double defaultValue) { 
		for (int i=0; i<_fields.length; i++) {
			if (field.equals(_fields[i])) {
				return _values[i];
			}
		}
		return defaultValue;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i=0; i<_fields.length; i++) {
			if (i > 0) {
				sb.append("|");
			}
			sb.append(_fields[i]);
			sb.append(":");
			sb.append(_values[i]);
		}
		return sb.toString();
	}

}
