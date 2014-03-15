package me.akuz.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONObject;
import org.json.JSONTokener;

import me.akuz.core.gson.GsonSerializers;

/**
 * JSON object.
 *
 */
public class JSONObj extends JSONObject {
	
	public static final JSONObj fromFile(String fileName, String encoding) throws FileNotFoundException, IOException {
		try (
			FileInputStream   fis = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(fis, encoding)) {

			JSONTokener tokener = new JSONTokener(isr);
			JSONObj obj = new JSONObj(tokener);
			return obj;
		}
	}
	
	private JSONObj(JSONTokener tokener) {
		super(tokener);
	}
	
	@Override
	public String toString() {
		return GsonSerializers.getNoHtmlEscapingPretty().toJson(this);
	}
}
