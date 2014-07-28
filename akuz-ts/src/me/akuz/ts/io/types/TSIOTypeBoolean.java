package me.akuz.ts.io.types;

import java.io.IOException;


import com.google.gson.JsonObject;

public final class TSIOTypeBoolean extends TSIOType {

	@Override
	public Object fromJson(JsonObject obj, String name) {
		if (!obj.has(name)) {
			return null;
		}
		return obj.get(name).getAsBoolean();
	}

	@Override
	public void setJsonField(JsonObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		obj.addProperty(name, (Boolean)value);
	}
	
	@Override
	public String toString(Object value) {
		if (value == null) {
			return NullString;
		}
		return ((Boolean)value).toString();
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		if (NullString.equals(str.trim())) {
			return null;
		} else {
			return Boolean.parseBoolean(str);
		}
	}
}
