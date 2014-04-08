package me.akuz.nlp.ontology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;

/**
 * Model containing all {@link Entity}s to be detected.
 *
 */
public final class EntityModel {
	
	private final Set<String> _stems;
	private final List<Entity> _patterns;
	
	public EntityModel() {
		_stems = new HashSet<>();
		_patterns = new ArrayList<>();
	}
	
	public void addEntities(JsonArray entities) {

		for (int i=0; i<entities.size(); i++) {
			
			Entity entity = new Entity(entities.get(i).getAsJsonObject());
			if (_stems.contains(entity.getStem())) {
				throw new IllegalArgumentException("Duplicate entity stem: " + entity.getStem());
			}
			_stems.add(entity.getStem());
			_patterns.add(entity);
		}
	}
	
	public Set<String> getStems() {
		return _stems;
	}
	
	public List<Entity> getEntities() {
		return _patterns;
	}

}
