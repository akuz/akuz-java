package me.akuz.nlp.ontology;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Collection of {@link Topic}s with the same starting stem.
 *
 */
public final class TopicColl {
	
	private static final String StemField = "stem";
	private static final String ListField = "list";
	
	private final JsonObject _obj;
	
	public TopicColl(JsonObject obj) {
		_obj = obj;
	}
	
	public String getStem() {
		return _obj.get(StemField).getAsString();
	}
	
	public JsonArray getList() {
		return _obj.get(ListField).getAsJsonArray();
	}
	
	public void completeAfterLoading() {
		String parentStem = getStem();
		JsonArray list = getList();
		for (int i=0; i<list.size(); i++) {
			Topic topic = new Topic(list.get(i).getAsJsonObject());
			topic.setStem(parentStem + "/" + topic.getStem());
		}
	}

}
