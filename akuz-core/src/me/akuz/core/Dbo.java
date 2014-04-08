package me.akuz.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Dbo {
	
	private final Map<String, Object> _map;
	
	public Dbo() {
		_map = new HashMap<>();
	}
	
	public Dbo(Map<String, Object> map) {
		_map = map;
	}
	
	public Map<String, Object> getMap() {
		return _map;
	}
	
	public void set(String field, Object value) {
		_map.put(field, value);
	}
	
	public Object get(String field) {
		return _map.get(field);
	}
	
	public Integer getInteger(String field) {
		return (Integer)_map.get(field);
	}
	
	public Long getLong(String field) {
		return (Long)_map.get(field);
	}
	
	public Double getDouble(String field) {
		return (Double)_map.get(field);
	}
	
	public String getString(String field) {
		return (String)_map.get(field);
	}
	
	public boolean getBoolean(String field) {
		Boolean is = (Boolean)_map.get(field);
		return is != null && is.booleanValue();
	}
	
	public Date getDate(String field) {
		return (Date)_map.get(field);
	}
	
	public Level getLogLevel(String field) {
		return (Level)_map.get(field);
	}

}
