package me.akuz.ts.io.types;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import me.akuz.core.DateFmt;
import me.akuz.ts.io.IOType;

import com.google.gson.JsonObject;

public final class TDate extends IOType {

	public TDate() {
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
		final Date date = (Date)value;
		final String str = DateFmt.format(date, DateFmt.StandardUtcFormat, DateFmt.UTC_TIMEZONE);
		return str;
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		try {
			final Date date = DateFmt.parse(str, DateFmt.StandardUtcFormat, DateFmt.UTC_TIMEZONE);
			return date;
		} catch (ParseException e) {
			throw new IOException("Could not parse date '" + str + "'", e);
		}
	}
}
