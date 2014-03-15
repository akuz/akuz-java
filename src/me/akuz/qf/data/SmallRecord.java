package me.akuz.qf.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.akuz.core.Pair;


/**
 * Data record optimized for small number of fields.
 * 
 */
public class SmallRecord {
	
	private final List<Pair<Integer, Object>> _data;
	
	public SmallRecord(int size) {
		_data = new ArrayList<>(size);
	}
	
	public void set(Integer key, Object value) {
		Pair<Integer, Object> pair = null;
		for (int i=0; i<_data.size(); i++) {
			Pair<Integer, Object> p = _data.get(i);
			if (p.v1().equals(key)) {
				pair = p;
				break;
			}
		}
		if (pair != null) {
			pair.setV2(value);
		} else {
			pair = new Pair<Integer, Object>(key, value);
			_data.add(pair);
		}
	}
	
	public Date getDate(Integer key) {
		for (int i=0; i<_data.size(); i++) {
			Pair<Integer, Object> p = _data.get(i);
			if (p.v1().equals(key)) {
				return (Date)p.v2();
			}
		}
		return null;
	}
	
	public Integer getInteger(Integer key) {
		for (int i=0; i<_data.size(); i++) {
			Pair<Integer, Object> p = _data.get(i);
			if (p.v1().equals(key)) {
				return (Integer)p.v2();
			}
		}
		return null;
	}
	
	public Double getDouble(Integer key) {
		for (int i=0; i<_data.size(); i++) {
			Pair<Integer, Object> p = _data.get(i);
			if (p.v1().equals(key)) {
				return (Double)p.v2();
			}
		}
		return null;
	}

}
