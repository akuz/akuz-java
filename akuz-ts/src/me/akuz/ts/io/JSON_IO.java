package me.akuz.ts.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.akuz.ts.TItem;
import me.akuz.ts.TFrame;
import me.akuz.ts.TSeq;
import me.akuz.ts.align.TSAlignIterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * JSON time series IO functions.
 *
 */
public final class JSON_IO {
	
	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TSeq<T> seq,
			final String timeFieldName, 
			final IOType timeDataType,
			final K key,
			final IOType dataType) {
		
		TFrame<K, T> frame = new TFrame<>();
		frame.addSeq(key, seq);
		
		IOMap<K> ioMap = new IOMap<>(
				timeFieldName, 
				timeDataType, 
				frame, 
				dataType);
		
		return toJson(frame, ioMap);
	}

	/**
	 * Serialize a frame according to provided TSIOMap.
	 */
	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TFrame<K, T> frame,
			final IOMap<K> tsioMap) {
		
		final JsonArray jsonArr = new JsonArray();
		
		final Set<T> timeSet = new HashSet<>();
		frame.extractTimes(timeSet);
		
		final List<K> keys = tsioMap.getKeys();

		final TSAlignIterator<K, T> iterator = new TSAlignIterator<K, T>(frame, timeSet, keys);
		while (iterator.hasNext()) {
			
			final Map<K, TItem<T>> currKeyItems = iterator.next();

			final JsonObject jsonObj = new JsonObject();
			
			tsioMap.getTimeDataType().toJsonField(
					jsonObj, 
					tsioMap.getTimeFieldName(), 
					iterator.getCurrTime());

			for (int j=0; j<keys.size(); j++) {
				
				final K key = keys.get(j);

				final TItem<T> item = currKeyItems.get(key);
				if (item == null) {
					continue;
				}
				
				tsioMap.getDataType(key).toJsonField(
						jsonObj, 
						tsioMap.getFieldName(key), 
						item.getObject());
			}

			jsonArr.add(jsonObj);
		}
		return jsonArr;
	}

	public static final <K, T extends Comparable<T>> TFrame<K,T> fromJson(
			final JsonArray jsonArr,
			final IOMap<K> tsioMap) throws IOException {
		
		TFrame<K, T> frame = new TFrame<>();
		
		if (jsonArr.size() > 0) {
			
			for (int i=0; i<jsonArr.size(); i++) {
				
				JsonObject jsonObj = jsonArr.get(i).getAsJsonObject();
				
				@SuppressWarnings("unchecked")
				T time = (T)tsioMap.getTimeDataType().fromJsonField(
						jsonObj, 
						tsioMap.getTimeFieldName());
				
				if (time == null) {
					throw new IOException("Data item #" + (i+1) + " does not have a time field '" + tsioMap.getTimeFieldName() + "'");
				}
				
				for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {

					final String fieldName = entry.getKey();
					final K key = tsioMap.getKey(fieldName);
					if (key != null) {

						final Object value = tsioMap.getDataType(key).fromJsonField(jsonObj, fieldName);
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
