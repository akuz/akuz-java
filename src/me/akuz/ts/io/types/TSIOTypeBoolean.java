package me.akuz.ts.io.types;

import me.akuz.ts.io.TSIOType;

import org.json.JSONObject;

public final class TSIOTypeBoolean extends TSIOType {

	@Override
	public Object fromJson(JSONObject obj, String name) {
		if (!obj.has(name)) {
			return null;
		}
		return obj.getBoolean(name);
	}

	@Override
	public void setJsonField(JSONObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		obj.put(name, (Boolean)value);
	}
}
