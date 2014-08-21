package me.akuz.ts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Quote containing values in one or more fields.
 *
 */
public final class Quote {
	
	private final QuoteField[] _fields;
	private final Object[] _values;
	
	/**
	 * Quote builder for optimizing the data use.
	 *
	 */
	public final static class Builder {
		
		private Map<QuoteField, Object>  _values;
		
		private Builder() {
			_values = new HashMap<>();
		}
		
		/**
		 * Set quote field value.
		 * 
		 */
		public Builder set(final QuoteField field, final Object value) {
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
			final Object[] values = new Object[_values.size()];
			int i = 0;
			for (Entry<QuoteField, Object> entry : _values.entrySet()) {
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
			final Object[] values) {
		
		_fields = fields;
		_values = values;
	}
	
	/**
	 * Check field is populated or not.
	 * 
	 */
	public boolean has(final QuoteField field) { 
		for (int i=0; i<_fields.length; i++) {
			if (field.equals(_fields[i])) {
				return true;
			}
		}
		return false;
	}
	
	public Date getDate(final QuoteField field) {
		return (Date)getObject(field);
	}
	public Date getDate(final QuoteField field, final Date defaultValue) {
		return (Date)getObject(field, defaultValue);
	}
	
	public Boolean getBoolean(final QuoteField field) {
		return (Boolean)getObject(field);
	}
	public Boolean getBoolean(final QuoteField field, final Boolean defaultValue) {
		return (Boolean)getObject(field, defaultValue);
	}
	
	public Integer getInteger(final QuoteField field) {
		return (Integer)getObject(field);
	}
	public Integer getInteger(final QuoteField field, final Integer defaultValue) {
		return (Integer)getObject(field, defaultValue);
	}
	
	public Double getDouble(final QuoteField field) {
		return (Double)getObject(field);
	}
	public Double getDouble(final QuoteField field, final Double defaultValue) {
		return (Double)getObject(field, defaultValue);
	}
	
	/**
	 * Get field value, throw exception if missing.
	 * 
	 */
	public Object getObject(final QuoteField field) { 
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
	public Object getObject(final QuoteField field, final Object defaultValue) { 
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
