package me.akuz.ts.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.akuz.ts.TSAlignIterator;
import me.akuz.ts.TSEntry;
import me.akuz.ts.TSInputMap;
import me.akuz.ts.TSMap;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Time series IO functions.
 *
 */
public final class TSIO {

	public static final <K, T extends Comparable<T>> JSONObject toJson(TSIOType timeType, TSIOMap<K,T> ioMap, TSMap<K, T> tsMap) {
		
		final JSONObject obj = new JSONObject();
		
		final JSONArray data = new JSONArray();

		final List<T> times = new ArrayList<>(tsMap.getTimes());
		if (times.size() > 1) {
			Collections.sort(times);
		}
		
		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(tsMap.getMap(), times, ioMap.getKeys());
		while (iterator.hasNext()) {
			
			final Map<K, TSEntry<T>> currKeyEntries = iterator.next();
			
			final JSONObject item = new JSONObject();

			for (final Entry<K, TSEntry<T>> keyEntry : currKeyEntries.entrySet()) {
				
				ioMap.setJsonField(keyEntry.getValue(), keyEntry.getKey(), item);
			}
			
			timeType.setJsonField(item, Field.time, iterator.getCurrTime());
			
			data.put(item);
		}
		
		obj.put(Field.data, data);
		
		return obj;
	}

	public static final <K, T extends Comparable<T>> TSMap<K,T> fromJson(TSIOType timeType, TSIOMap<K,T> ioMap, JSONObject obj) throws IOException {
		
		TSInputMap<K,T> tsMap = new TSInputMap<>();
		
		if (obj.has(Field.data)) {
			final JSONArray data = obj.getJSONArray(Field.data);
			if (data != null && data.length() > 0) {
				
				for (int i=0; i<data.length(); i++) {
					JSONObject item = data.getJSONObject(i);
					@SuppressWarnings("unchecked")
					T time = (T)timeType.fromJson(item, Field.time);
					if (time == null) {
						throw new IOException("Data item " + (i+1) + " does not have time field '" + Field.time + "'");
					}
					String[] names = JSONObject.getNames(item);
					if (names != null && names.length > 0) {
						for (int j=0; j<names.length; j++) {
							String name = names[j];
							K key = ioMap.getKey(name);
							if (key != null) {
								Object value = ioMap.fromJson(item, name);
								if (value != null) {
									tsMap.add(key, new TSEntry<T>(time, value));
								}
							}
						}
					}
				}
			}
		}

		return tsMap;
	}
}
