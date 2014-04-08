package me.akuz.ts.io.types;

import java.io.IOException;

import me.akuz.core.Stringable;
import me.akuz.ts.io.TSIOType;

import com.google.gson.JsonObject;

public final class TSIOTypeStringable extends TSIOType {

	private final Stringable _template;
	
	public TSIOTypeStringable(Stringable template) {
		_template = template;
	}

	@Override
	public Object fromJson(JsonObject obj, String name) throws IOException {
		if (!obj.has(name)) {
			return null;
		}
		String str = obj.get(name).getAsString();
		return _template.convertFromString(str);
	}

	@Override
	public void setJsonField(JsonObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		Stringable stringable = (Stringable)value;
		obj.addProperty(name, stringable.convertToString());
	}
}
