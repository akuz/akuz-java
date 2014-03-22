package me.akuz.ts.io.types;

import me.akuz.ts.io.TSIOType;

import org.json.JSONObject;

public final class TSIOTypeDouble extends TSIOType {

	@Override
	public Object fromJson(JSONObject obj, String name) {
		if (!obj.has(name)) {
			return null;
		}
		return obj.getDouble(name);
	}

	@Override
	public void setJsonField(JSONObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		obj.put(name, (Double)value);
	}
}
