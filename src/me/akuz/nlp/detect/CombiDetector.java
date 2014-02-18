package me.akuz.nlp.detect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.akuz.core.Hit;
import me.akuz.core.HitsUtils;

/**
 * Combination detector that first detects entities, and 
 * then remaining words that do not overlap with entities.
 *
 */
public final class CombiDetector {
	
	private final EntitiesDetector _entitiesDetector;
	private final WordsDetector _wordsDetector;
	
	public CombiDetector(EntitiesDetector entitiesDetector, WordsDetector wordsDetector) {
		_entitiesDetector = entitiesDetector;
		_wordsDetector = wordsDetector;
	}
	
	public Map<String, List<Hit>> extractHitsByStem(String str, Hit bounds) {
		
		Map<String, List<Hit>> resultMap = null;
		
		Map<Integer, List<Hit>> entityHits = _entitiesDetector.extractHitsByEntityIndex(str, bounds);
		if (entityHits != null && entityHits.size() > 0) {
			
			for (Entry<Integer, List<Hit>> entry : entityHits.entrySet()) {
				
				String stem = _entitiesDetector.getEntity(entry.getKey()).getStem();
				List<Hit> hits = entry.getValue();

				for (int i=0; i<hits.size(); i++) {
					
					Hit hit = hits.get(i);
					if (resultMap == null) {
						resultMap = new HashMap<>();
					}
					List<Hit> resultList = resultMap.get(stem);

					if (resultList == null) {
						resultList = new ArrayList<>();
						resultMap.put(stem, resultList);
					}
					
					resultList.add(hit);
				}
			}
		}
		
		Map<String, List<Hit>> wordHits = _wordsDetector.extractHitsByStem(str, bounds);
		if (wordHits != null && wordHits.size() > 0) {
			
			for (Entry<String, List<Hit>> entry : wordHits.entrySet()) {
				
				String stem = entry.getKey();
				List<Hit> hits = entry.getValue();
				
				for (int i=0; i<hits.size(); i++) {
					
					Hit hit = hits.get(i);
					
					if (HitsUtils.overlapsAny(entityHits, hit) == false) {
						
						if (resultMap == null) {
							resultMap = new HashMap<>();
						}
						List<Hit> resultList = resultMap.get(stem);
						if (resultList == null) {
							resultList = new ArrayList<>();
							resultMap.put(stem, resultList);
						}
						resultList.add(hit);
					}
				}
			}
		}
		
		return resultMap;
	}

}
