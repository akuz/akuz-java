package me.akuz.ts.io.types;

import java.io.IOException;

import me.akuz.core.DateAK;
import me.akuz.ts.io.IOType;

import com.google.gson.JsonObject;

public final class TDateAK extends IOType {

	public TDateAK() {
	}

	@Override
	public void toJsonField(JsonObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		final String str = toString(value);
		obj.addProperty(name, str);
	}

	@Override
	public Object fromJsonField(JsonObject obj, String name) throws IOException {
		if (!obj.has(name)) {
			return null;
		}
		String str = obj.get(name).getAsString();
		return fromString(str);
	}
	
	@Override
	public String toString(Object value) {
		if (value == null) {
			return null;
		}
		final DateAK date = (DateAK)value;
		return date.toString();
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		return new DateAK(str);
	}
}
