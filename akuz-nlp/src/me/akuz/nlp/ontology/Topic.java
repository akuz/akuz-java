package me.akuz.nlp.ontology;

import me.akuz.core.gson.GsonSerializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Topic inferred from some collection of texts; can be used for topic detection.
 *
 */
public final class Topic {
	
	public static final String StemField  = "stem";
	public static final String ProbField  = "prob";
	public static final String WordsField = "words";
	
	private final JsonObject _obj;
	
	public Topic(JsonObject obj) {
		_obj = obj;
	}
	
	public String getStem() {
		return _obj.get(StemField).getAsString();
	}
	
	public void setStem(String stem) {
		_obj.addProperty(StemField, stem);
	}

	public double getProb() {
		return _obj.get(ProbField).getAsDouble();
	}
	
	public JsonArray getWords() {
		return _obj.get(WordsField).getAsJsonArray();
	}
	
	@Override
	public String toString() {
		return GsonSerializers.getNoHtmlEscapingPretty().toJson(_obj);
	}

}
