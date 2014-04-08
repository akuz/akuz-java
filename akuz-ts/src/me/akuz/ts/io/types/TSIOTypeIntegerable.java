package me.akuz.ts.io.types;

import java.io.IOException;

import me.akuz.core.Integerable;
import me.akuz.ts.io.TSIOType;

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
}
