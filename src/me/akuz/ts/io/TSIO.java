package me.akuz.ts.io;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.akuz.ts.TS;
import me.akuz.ts.TSItem;
import me.akuz.ts.TSMap;
import me.akuz.ts.TSSortMap;
import me.akuz.ts.align.TSAlignIterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Time series IO functions.
 *
 */
public final class TSIO {


	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TS<T> ts, 
			final String fieldName,
			final TSIOType timeType, 
			final TSIOType valueType) {
		
		final JsonArray data = new JsonArray();

		List<TSItem<T>> tsItems = ts.getItems();
		for (int i=0; i<tsItems.size(); i++) {
			
			TSItem<T> tsItem = tsItems.get(i);

			final JsonObject item = new JsonObject();
			timeType.setJsonField(item, TSIOField.time, tsItem.getTime());
			valueType.setJsonField(item, fieldName, tsItem.getObject());
			data.add(item);
		}
		
		return data;
	}

	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TSMap<K, T> tsMap, 
			final TSIOType timeType, 
			final TSIOType tsioType) {
		
		TSIOMap<K, T> tsioMap = new TSIOMap<>(tsMap, tsioType);
		return toJson(tsMap, timeType, tsioMap);
	}

	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TSMap<K, T> tsMap, 
			final TSIOType timeType, 
			final TSIOMap<K,T> ioMap) {
		
		final JsonArray data = new JsonArray();

		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(tsMap.getMap(), tsMap.getTimes(), ioMap.getKeys());
		while (iterator.hasNext()) {
			
			final Map<K, TSItem<T>> currKeyEntries = iterator.next();
			
			final JsonObject item = new JsonObject();
			
			timeType.setJsonField(item, TSIOField.time, iterator.getCurrTime());

			for (final Entry<K, TSItem<T>> keyEntry : currKeyEntries.entrySet()) {
				
				ioMap.setJsonField(keyEntry.getValue(), keyEntry.getKey(), item);
			}
			
			data.add(item);
		}
		
		return data;
	}

	public static final <K, T extends Comparable<T>> TSMap<K,T> fromJson(
			final JsonArray data, 
			final TSIOType timeType, 
			final TSIOMap<K,T> ioMap) throws IOException {
		
		TSSortMap<K, T> tsSortMap = new TSSortMap<>();
		
		if (data.size() > 0) {
			
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
							tsSortMap.add(key, new TSItem<T>(time, value));
						}
					}
				}
			}
		}
		
		TSMap<K, T> tsMap = tsSortMap.build();
		return tsMap;
	}
}
