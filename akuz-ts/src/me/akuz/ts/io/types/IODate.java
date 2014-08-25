package me.akuz.ts.io.types;

import java.io.IOException;

import me.akuz.core.TDate;
import me.akuz.ts.io.IOType;

import com.google.gson.JsonObject;

public final class IODate extends IOType {

	public IODate() {
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
		final TDate date = (TDate)value;
		return date.toString();
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		return new TDate(str);
	}
}
