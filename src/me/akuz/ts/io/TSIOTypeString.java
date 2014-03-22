package me.akuz.ts.io;

import org.json.JSONObject;

public final class TSIOTypeString extends TSIOType {

	@Override
	public Object fromJson(JSONObject obj, String name) {
		if (!obj.has(name)) {
			return null;
		}
		return obj.getString(name);
	}

	@Override
	public void setJsonField(JSONObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		obj.put(name, (String)value);
	}
}
