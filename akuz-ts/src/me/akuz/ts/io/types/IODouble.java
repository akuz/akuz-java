package me.akuz.ts.io.types;

import java.io.IOException;

import me.akuz.ts.io.IOType;

import com.google.gson.JsonObject;

public final class IODouble extends IOType {

	@Override
	public Object fromJsonField(JsonObject obj, String name) {
		if (!obj.has(name)) {
			return null;
		}
		return obj.get(name).getAsDouble();
	}

	@Override
	public void toJsonField(JsonObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		obj.addProperty(name, (Double)value);
	}

	@Override
	public String toString(Object value) {
		if (value == null) {
			return null;
		}
		return ((Double)value).toString();
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			throw new IOException("Could not parse double '" + str + "'", e);
		}
	}
}
