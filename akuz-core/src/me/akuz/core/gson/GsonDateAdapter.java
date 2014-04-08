package me.akuz.core.gson;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

public final class GsonDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
	
	private final DateFormat dateFormat;
	
	public GsonDateAdapter() {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public synchronized JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
		synchronized (dateFormat) {
			String dateFormatAsString = dateFormat.format(date);
			return new JsonPrimitive(dateFormatAsString);
		}
	}

	public synchronized Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
		try {
			synchronized (dateFormat) {
				return dateFormat.parse(jsonElement.getAsString());
			}
		} catch (ParseException e) {
			throw new JsonSyntaxException(jsonElement.getAsString(), e);
		}
	}
}