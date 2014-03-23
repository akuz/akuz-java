package me.akuz.ts.io.types;

import me.akuz.ts.io.TSIOType;

import com.google.gson.JsonObject;

public final class TSIOTypeDouble extends TSIOType {

	@Override
	public Object fromJson(JsonObject obj, String name) {
		if (!obj.has(name)) {
			return null;
		}
		return obj.get(name).getAsDouble();
	}

	@Override
	public void setJsonField(JsonObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		obj.addProperty(name, (Double)value);
	}
}
