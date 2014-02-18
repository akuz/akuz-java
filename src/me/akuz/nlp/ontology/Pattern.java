package me.akuz.nlp.ontology;

import me.akuz.core.gson.GsonSerializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Pattern that can be detected in text, possibly with multiple different regular expressions.
 *
 */
public final class Pattern {
	
	public static final String NameField = "name";
	public static final String StemField = "stem";
	public static final String RegxField = "regx";
	
	private final JsonObject _obj;
	
	public Pattern(JsonObject obj) {
		_obj = obj;
	}
	
	public String getName() {
		return _obj.get(NameField).getAsString();
	}
	
	public String getStem() {
		return _obj.get(StemField).getAsString();
	}
	
	public JsonArray getRegx() {
		return _obj.get(RegxField).getAsJsonArray();
	}

	@Override
	public String toString() {
		return GsonSerializers.getNoHtmlEscapingPretty().toJson(_obj);
	}

}
