package me.akuz.ts.io.types;

import java.io.IOException;

import me.akuz.core.DateTimeAK;
import me.akuz.ts.io.IOType;

import com.google.gson.JsonObject;

public final class TDateTimeAK extends IOType {

	public TDateTimeAK() {
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
		final DateTimeAK date = (DateTimeAK)value;
		return date.toString();
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		return DateTimeAK.parse(str);
	}
}
