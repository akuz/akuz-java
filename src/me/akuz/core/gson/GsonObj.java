package me.akuz.core.gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * JSON object.
 *
 */
public class GsonObj {
	
	private final JsonObject _obj;
	
	public GsonObj() {
		_obj = new JsonObject();
	}
	
	public GsonObj(Reader json) {
		_obj = new JsonParser().parse(json).getAsJsonObject();
	}
	
	public GsonObj(String json) {
		_obj = new JsonParser().parse(json).getAsJsonObject();
	}
	
	public GsonObj(String fileName, String encoding) throws FileNotFoundException, IOException {

		try (FileInputStream fis = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(fis, encoding);) {
			
			_obj = new JsonParser().parse(isr).getAsJsonObject();
		}
	}
	
	public JsonObject get() {
		return _obj;
	}
	
	public void set(final String key, final String value) {
		_obj.addProperty(key, value);
	}
	public void set(final String key, final int value) {
		_obj.addProperty(key, value);
	}
	public void set(final String key, final double value) {
		_obj.addProperty(key, value);
	}
	public void set(final String key, final boolean value) {
		_obj.addProperty(key, value);
	}
	public void set(final String key, final JsonElement value) {
		_obj.add(key, value);
	}
	
	public String getString(final String key) {
		return _obj.get(key).getAsString();
	}
	public int getInt(final String key) {
		return _obj.get(key).getAsInt();
	}
	public double getDouble(final String key) {
		return _obj.get(key).getAsDouble();
	}
	public boolean getBool(final String key) {
		return _obj.get(key).getAsBoolean();
	}
	public JsonElement get(final String key) {
		return _obj.get(key);
	}
	
	public Set<String> getStringSet(final String key) {
		Set<String> set = null;
		if (_obj.has(key)) {
			
			set = new HashSet<>();
			
			JsonArray arr = _obj.get(key).getAsJsonArray();
			for (int i=0; i<arr.size(); i++) {
				set.add(arr.get(i).getAsString());
			}
		}
		
		return set;
	}
	
	@Override
	public String toString() {
		return GsonSerializers.getNoHtmlEscapingPretty().toJson(_obj);
	}
}
