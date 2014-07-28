package me.akuz.ts.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.akuz.ts.TSeq;
import me.akuz.ts.TItem;
import me.akuz.ts.TFrame;
import me.akuz.ts.align.TSAlignIterator;
import me.akuz.ts.io.types.TSIOType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * JSON time series IO functions.
 *
 */
public final class JSON_TSIO {

	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TSeq<T> seq, 
			final String timeFieldName,
			final TSIOType timeDataType,
			final String valueFieldName, 
			final TSIOType valueDataType) {
		
		final JsonArray data = new JsonArray();

		List<TItem<T>> items = seq.getItems();
		for (int i=0; i<items.size(); i++) {
			
			TItem<T> item = items.get(i);

			final JsonObject jsonItem = new JsonObject();
			timeDataType.setJsonField(jsonItem, timeFieldName, item.getTime());
			valueDataType.setJsonField(jsonItem, valueFieldName, item.getObject());
			data.add(jsonItem);
		}
		
		return data;
	}

	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TFrame<K, T> frame, 
			final String timeFieldName,
			final TSIOType timeDataType, 
			final TSIOType valuesDataType) {
		
		TSIOMap<K, T> tsioMap = new TSIOMap<>(frame, valuesDataType);
		return toJson(frame, timeFieldName, timeDataType, tsioMap);
	}

	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TFrame<K, T> frame,
			final String timeFieldName,
			final TSIOType timeDataType, 
			final TSIOMap<K,T> tsioMap) {
		
		final JsonArray data = new JsonArray();
		
		final Set<T> times = new HashSet<>();
		frame.extractTimes(times);

		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(frame, times, tsioMap.getKeys());
		while (iterator.hasNext()) {
			
			final Map<K, TItem<T>> currKeyEntries = iterator.next();
			final JsonObject item = new JsonObject();
			timeDataType.setJsonField(item, timeFieldName, iterator.getCurrTime());
			for (final Entry<K, TItem<T>> keyEntry : currKeyEntries.entrySet()) {
				tsioMap.setJsonField(keyEntry.getValue(), keyEntry.getKey(), item);
			}
			data.add(item);
		}
		return data;
	}

	public static final <K, T extends Comparable<T>> TFrame<K,T> fromJson(
			final JsonArray data,
			final String timeFieldName,
			final TSIOType timeDataType, 
			final TSIOMap<K,T> ioMap) throws IOException {
		
		TFrame<K, T> frame = new TFrame<>();
		
		if (data.size() > 0) {
			
			for (int i=0; i<data.size(); i++) {
				JsonObject item = data.get(i).getAsJsonObject();
				@SuppressWarnings("unchecked")
				T time = (T)timeDataType.fromJson(item, timeFieldName);
				if (time == null) {
					throw new IOException("Data item " + (i+1) + " does not have time field '" + timeFieldName + "'");
				}
				for (Entry<String, JsonElement> entry : item.entrySet()) {
					String name = entry.getKey();
					K key = ioMap.getKey(name);
					if (key != null) {
						Object value = ioMap.fromJson(item, name);
						if (value != null) {
							frame.stage(key, new TItem<T>(time, value));
						}
					}
				}
			}
		}
		
		frame.acceptStaged();
		return frame;
	}
}
