package me.akuz.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import me.akuz.core.gson.GsonSerializers;

/**
 * JSON object.
 *
 */
public class JSONObj extends JSONObject {
	
	public JSONObj() {
		super();
	}
	
	public JSONObj(JSONTokener tokener) {
		super(tokener);
	}
	
	public static final JSONObj fromFile(String fileName) throws FileNotFoundException, IOException {
		return fromFile(fileName, "UTF-8");
	}
	
	public static final JSONObj fromFile(String fileName, String encoding) throws FileNotFoundException, IOException {
		
		try (FileInputStream fis = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(fis, encoding)) {

			JSONTokener tokener = new JSONTokener(isr);
			JSONObj obj = new JSONObj(tokener);
			return obj;
		}
	}
	
	public Set<String> getStringSet(final String key) {
		
		Set<String> set = null;
		JSONArray arr = getJSONArray(key);
		if (arr != null) {
			
			set = new HashSet<>();
			for (int i=0; i<arr.length(); i++) {
				set.add(arr.getString(i));
			}
		}
		return set;
	}
	
	@Override
	public String toString() {
		return GsonSerializers.getNoHtmlEscapingPretty().toJson(this);
	}
}
