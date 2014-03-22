package me.akuz.ts.io;

import org.json.JSONObject;

public final class TSIOTypeInteger extends TSIOType {

	@Override
	public Object fromJson(JSONObject obj, String name) {
		if (!obj.has(name)) {
			return null;
		}
		return obj.getInt(name);
	}

	@Override
	public void setJsonField(JSONObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		obj.put(name, (Integer)value);
	}
}
