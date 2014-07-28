package me.akuz.ts.io.types;

import java.io.IOException;

import me.akuz.core.Integerable;

import com.google.gson.JsonObject;

public final class TSIOTypeIntegerable extends TSIOType {

	private final Integerable _template;
	
	public TSIOTypeIntegerable(Integerable template) {
		_template = template;
	}

	@Override
	public Object fromJson(JsonObject obj, String name) throws IOException {
		if (!obj.has(name)) {
			return null;
		}
		Integer num = obj.get(name).getAsInt();
		return _template.convertFromInteger(num);
	}

	@Override
	public void setJsonField(JsonObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		Integerable integerable = (Integerable)value;
		obj.addProperty(name, integerable.convertToInteger());
	}

	@Override
	public String toString(Object value) {
		if (value == null) {
			return NullString;
		}
		return ((Integerable)value).convertToInteger().toString();
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		if (NullString.equals(str.trim())) {
			return null;
		} else {
			final Integer num;
			try {
				num = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				throw new IOException("Could not parse integer '" + str + "'", e);
			}
			return _template.convertFromInteger(num);
		}
	}
}
