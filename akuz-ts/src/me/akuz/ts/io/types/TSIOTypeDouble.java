package me.akuz.ts.io.types;

import java.io.IOException;

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

	@Override
	public String toString(Object value) {
		if (value == null) {
			return NullString;
		}
		return ((Double)value).toString();
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		if (NullString.equals(str.trim())) {
			return null;
		} else {
			try {
				return Double.parseDouble(str);
			} catch (NumberFormatException e) {
				throw new IOException("Could not parse double '" + str + "'", e);
			}
		}
	}
}
