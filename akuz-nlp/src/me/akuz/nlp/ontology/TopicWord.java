package me.akuz.nlp.ontology;

import me.akuz.core.gson.GsonSerializers;

import com.google.gson.JsonObject;

/**
 * Topic word, an item in the {@link Topic} words collection.
 *
 */
public final class TopicWord {

	public static final String WordField = "word";
	public static final String StemField = "stem";
	public static final String ProbField = "prob";
	
	private final JsonObject _obj;
	
	public TopicWord(JsonObject obj) {
		_obj = obj;
	}
	
	public String getWord() {
		return _obj.get(WordField).getAsString();
	}
	
	public String getStem() {
		return _obj.get(StemField).getAsString();
	}
	
	public double getProb() {
		return _obj.get(ProbField).getAsDouble();
	}
	
	@Override
	public String toString() {
		return GsonSerializers.getNoHtmlEscapingPretty().toJson(_obj);
	}

}
