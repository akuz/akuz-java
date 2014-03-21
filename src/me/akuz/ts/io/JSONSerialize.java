package me.akuz.ts.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import me.akuz.ts.TSAlignIterator;
import me.akuz.ts.TSEntry;
import me.akuz.ts.TSMap;

public final class JSONSerialize {

	public static final <K, T extends Comparable<T>> JSONObject serialize(Set<K> keys, TSMap<K, T> tsMap) {
		
		final JSONObject obj = new JSONObject();
		
		final JSONArray data = new JSONArray();

		final List<T> times = new ArrayList<>(tsMap.getTimes());
		if (times.size() > 1) {
			Collections.sort(times);
		}
		
		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(tsMap.getMap(), times, keys);
		while (iterator.hasNext()) {
			
			final Map<K, TSEntry<T>> currKeyEntries = iterator.next();
			
			final JSONObject dataEntry = new JSONObject();
			
			dataEntry.put("t", iterator.getCurrTime());

			for (final Entry<K, TSEntry<T>> keyEntry : currKeyEntries.entrySet()) {
				
				dataEntry.put(keyEntry.getKey().toString(), keyEntry.getValue().getObject());
			}
			
			data.put(dataEntry);
		}
		
		obj.put(JSONField.data, data);
		
		return obj;
		
	}
}
