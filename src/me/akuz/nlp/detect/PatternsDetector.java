package me.akuz.nlp.detect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;

import me.akuz.core.Hit;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.nlp.ontology.Entity;
import me.akuz.nlp.ontology.EntityModel;

/**
 * Detects pattern entities occurrences.
 *
 */
public final class PatternsDetector {

	private final Monitor _monitor;
	private final List<Entity> _entities;
	private final List<Pair<Integer, Integer>> _entityCounts;
	private int _totalDetectedEntitiesCount;
	private int _nextOptimizationDetectedEntitiesCount;
	private final Pattern _entitiesRegex;
	
	public PatternsDetector(Monitor parentMonitor, EntityModel entityModel) {
		this(parentMonitor, entityModel, false);
	}

	public PatternsDetector(Monitor parentMonitor, EntityModel entityModel, boolean caseInsensitive) {
		
		_monitor = parentMonitor != null ? new LocalMonitor(this.getClass().getSimpleName(), parentMonitor) : null;
		_entities = entityModel.getEntities();
		_entityCounts = new ArrayList<Pair<Integer,Integer>>();
		_nextOptimizationDetectedEntitiesCount = 100;
		_totalDetectedEntitiesCount = 0;
		
		StringBuilder sb = new StringBuilder();
		for (int entityIndex=0; entityIndex<_entities.size(); entityIndex++) {

			Entity entity = _entities.get(entityIndex);
			
			// after non-letter or at start
			sb.append("(?<=(?:\\W|^))(?:");

			_entityCounts.add(new Pair<Integer, Integer>(entityIndex, 0));
			_entities.add(entity);
			entityIndex++;

			if (entityIndex > 0) {
				sb.append("|");
			}
			
			sb.append("(");

			String entityStem = entity.getStem();
			String entityStemRegex = entityStem.replaceAll("/", "\\/");
			sb.append("(?:");
			sb.append(entityStemRegex);
			sb.append(")");
			
			// check if entity has patterns
			JsonArray regx = entity.getRegx();
			if (regx != null) {
				for (int k=0; k<regx.size(); k++) {
					sb.append("|");
					sb.append("(?:");
					
					String pattern = regx.get(k).getAsString();
					// IMPORTANT: convert all capturing groups to non-capturing
					pattern = pattern.replaceAll("\\((?!\\?)", "(?:");
					sb.append(pattern);
					sb.append(")");
				}
			}
			
			sb.append(")");
			
			// followed by non-letter or end
			sb.append(")(?=(?:\\W|$))");
		}
		if (sb.length() > 0) {
			int flags = 0;
			if (caseInsensitive) {
				flags |= Pattern.CASE_INSENSITIVE;
			}
			_entitiesRegex = Pattern.compile(sb.toString(), flags);
		} else {
			_entitiesRegex = null;
		}
	}
	
	public Entity getEntity(int index) {
		return _entities.get(index);
	}

	public Map<Integer, List<Hit>> extractHitsByEntityIndex(String str, Hit bounds) {

		Map<Integer, List<Hit>> hitsByEntityIndex = null;
		
		if (_entitiesRegex != null && str != null) {

			Matcher matcher = _entitiesRegex.matcher(str);
			matcher.region(bounds.start(), bounds.end());
			while (matcher.find()) {
				
				int entityIndex = getMatchedPatternEntityIndex(matcher);
				int matchStart = matcher.start(entityIndex+1);
				int matchEnd = matcher.end(entityIndex+1);
				
				if (matchStart >= matchEnd) {
					// can't match empty strings
					// possibly bad pattern
					// for entity
					continue;
				}

				Hit hit = new Hit(matchStart, matchEnd);

				// register the hit
				if (hitsByEntityIndex == null) {
					hitsByEntityIndex = new HashMap<Integer, List<Hit>>();
				}
				List<Hit> hits = hitsByEntityIndex.get(entityIndex);
				if (hits == null) {
					hits = new ArrayList<Hit>();
					hitsByEntityIndex.put(entityIndex, hits);
				}
				hits.add(hit);
			}
		}
		
		return hitsByEntityIndex;
	}

	private int getMatchedPatternEntityIndex(Matcher matcher) {
		
		for (int i=0; i<_entityCounts.size(); i++) {
			Pair<Integer,Integer> pair = _entityCounts.get(i);
			
			Integer index = pair.v1();
			Integer count = pair.v2();
			
			String m = matcher.group(index + 1);
			if (m != null && m.length() > 0) {
			
				_totalDetectedEntitiesCount += 1;
				if (_totalDetectedEntitiesCount >= _nextOptimizationDetectedEntitiesCount) {

					// optimize the order of checking the entities
					Collections.sort(_entityCounts, new PairComparator<Integer, Integer>(SortOrder.Desc));
					_nextOptimizationDetectedEntitiesCount = (int)(1.5 * _nextOptimizationDetectedEntitiesCount);

					if (_monitor != null) {
						_monitor.write("Optimized entity detector!");
						_monitor.write("------ top entities ------");
						for (int k=0; k<10 && k<_entityCounts.size(); k++) {
							Pair<Integer,Integer> pair2 = _entityCounts.get(k);
							Integer index2 = pair2.v1();
							Integer count2 = pair2.v2();
							Entity entity = _entities.get(index2);
							_monitor.write("" + count2 + "\t" + entity.getStem());
						}
						_monitor.write("--------------------------");
					}
				}
				
				pair.setV2(count + 1); 
				return index;
			}
		}

		throw new IllegalStateException("Cannot find which entity was found by matcher");
	}
	
}
