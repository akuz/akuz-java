package me.akuz.nlp.ontology;

import me.akuz.core.gson.GsonSerializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Collection of {@link Entity}s with the same starting stem.
 *
 */
public final class EntityColl {
	
	private static final String StemField = "stem";
	private static final String ListField = "list";
	
	private final JsonObject _obj;
	
	public EntityColl(JsonObject obj) {
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
			Entity entity = new Entity(list.get(i).getAsJsonObject());
			entity.setStem(parentStem + "/" + entity.getStem());
		}
	}

	@Override
	public String toString() {
		return GsonSerializers.getNoHtmlEscapingPretty().toJson(_obj);
	}

}
