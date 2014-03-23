package me.akuz.ts.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.akuz.ts.TSItem;
import me.akuz.ts.TSMap;
import me.akuz.ts.TSBuildMap;
import me.akuz.ts.align.TSAlignIterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Time series IO functions.
 *
 */
public final class TSIO {

	public static final <K, T extends Comparable<T>> JsonObject toJson(TSIOType timeType, TSIOMap<K,T> ioMap, TSMap<K, T> tsMap) {
		
		final JsonObject obj = new JsonObject();
		
		final JsonArray data = new JsonArray();

		final List<T> times = new ArrayList<>(tsMap.getTimes());
		if (times.size() > 1) {
			Collections.sort(times);
		}
		
		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(tsMap.getMap(), times, ioMap.getKeys());
		while (iterator.hasNext()) {
			
			final Map<K, TSItem<T>> currKeyEntries = iterator.next();
			
			final JsonObject item = new JsonObject();
			
			timeType.setJsonField(item, TSIOField.time, iterator.getCurrTime());

			for (final Entry<K, TSItem<T>> keyEntry : currKeyEntries.entrySet()) {
				
				ioMap.setJsonField(keyEntry.getValue(), keyEntry.getKey(), item);
			}
			
			data.add(item);
		}
		
		obj.add(TSIOField.data, data);
		
		return obj;
	}

	public static final <K, T extends Comparable<T>> TSMap<K,T> fromJson(TSIOType timeType, TSIOMap<K,T> ioMap, JsonObject obj) throws IOException {
		
		TSBuildMap<K, T> tsSortBuilderMap = new TSBuildMap<>();
		
		if (obj.has(TSIOField.data)) {
			final JsonArray data = obj.getAsJsonArray(TSIOField.data);
			if (data != null && data.size() > 0) {
				
				for (int i=0; i<data.size(); i++) {
					JsonObject item = data.get(i).getAsJsonObject();
					@SuppressWarnings("unchecked")
					T time = (T)timeType.fromJson(item, TSIOField.time);
					if (time == null) {
						throw new IOException("Data item " + (i+1) + " does not have time field '" + TSIOField.time + "'");
					}
					for (Entry<String, JsonElement> entry : item.entrySet()) {
						String name = entry.getKey();
						K key = ioMap.getKey(name);
						if (key != null) {
							Object value = ioMap.fromJson(item, name);
							if (value != null) {
								tsSortBuilderMap.add(key, new TSItem<T>(time, value));
							}
						}
					}
				}
			}
		}
		
		TSMap<K, T> tsMap = tsSortBuilderMap.build();
		return tsMap;
	}
}
