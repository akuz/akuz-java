package me.akuz.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class HitsUtils {

	public static final boolean overlapsAny(List<Hit> list, Hit hit) {
		
		if (list == null) {
			return false;
		}
		for (int i=0; i<list.size(); i++) {
			if (list.get(i).overlaps(hit)) {
				return true;
			}
		}
		return false;
	}

	public static final boolean overlapsAny(Map<?, List<Hit>> map, Hit hit) {
		
		if (map == null || map.size() == 0) {
			return false;
		}
		for (List<Hit> hits : map.values()) {
			for (int i=0; i<hits.size(); i++) {
				if (hit.overlaps(hits.get(i))) {
					return true;
				}
			}
		}
		return false;
	}
	
	@SafeVarargs
	public static final List<Hit> flattenHitMaps(Map<String, List<Hit>>... mapArr) {
		
		List<Hit> result = new ArrayList<>();
		
		if (mapArr != null) {
			for (int i=0; i<mapArr.length; i++) {
				if (mapArr[i] != null) {
					for (List<Hit> list : mapArr[i].values()) {
						result.addAll(list);
					}
				}
			}
		}
		
		return result;
	}

	@SafeVarargs
	public static final List<Hit> getUniqueHits(List<Hit>... hitsArr) {
		
		List<Hit> sortedHits = new ArrayList<>();
		if (hitsArr != null) {
			for (int i=0; i<hitsArr.length; i++) {
				if (hitsArr[i] != null) {
					sortedHits.addAll(hitsArr[i]);
				}
			}
		}
		if (sortedHits.size() > 1) {
			Collections.sort(sortedHits, new HitsSorter());
		}
		
		List<Hit> uniqueHits = new ArrayList<>();
		for (int i=0; i<sortedHits.size(); i++) {
			
			Hit hit = sortedHits.get(i);
			
			if (uniqueHits.size() > 0) {
				if (uniqueHits.get(uniqueHits.size()-1).overlaps(hit)) {
					continue;
				}
			}
			
			uniqueHits.add(hit);
		}
		
		return uniqueHits;
	}


	public static boolean overlapsAny(List<Hit> list, Hit hit, int minIndex, int maxIndex) {
		if (list == null) {
			return false;
		}
		final int checkMinIndex = Math.max(0, minIndex);
		final int checkMaxIndex = Math.min(list.size()-1, maxIndex);
		for (int i=checkMinIndex; i<=checkMaxIndex; i++) {
			if (list.get(i).overlaps(hit)) {
				return true;
			}
		}
		return false;
	}
	
}
