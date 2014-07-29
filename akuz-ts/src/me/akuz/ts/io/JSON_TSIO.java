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

	/**
	 * Serialize a sequence to a JSON array.
	 */
	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TSeq<T> seq, 
			final String timeFieldName,
			final TSIOType timeDataType,
			final String valueFieldName, 
			final TSIOType valueDataType) {
		
		TFrame<String, T> frame = new TFrame<>();
		frame.addSeq(valueFieldName, seq);
		
		return toJson(
				frame,
				timeFieldName,
				timeDataType,
				valueDataType);
	}

	/**
	 * Serialize a frame, with the same data type 
	 * for all keys, to a JSON array.
	 */
	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TFrame<K, T> frame, 
			final String timeFieldName,
			final TSIOType timeDataType, 
			final TSIOType valueDataType) {
		
		TSIOMap<K> tsioMap = new TSIOMap<>(frame, valueDataType);
		
		return toJson(
				frame, 
				timeFieldName, 
				timeDataType, 
				tsioMap);
	}

	/**
	 * Serialize a frame according to provided TSIOMap.
	 */
	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final TFrame<K, T> frame,
			final String timeFieldName,
			final TSIOType timeDataType, 
			final TSIOMap<K> tsioMap) {
		
		final JsonArray jsonArr = new JsonArray();
		
		final Set<T> timeSet = new HashSet<>();
		frame.extractTimes(timeSet);
		List<K> keys = tsioMap.getKeys();

		final TSAlignIterator<K, T> iterator = new TSAlignIterator<K, T>(frame, timeSet, keys);
		while (iterator.hasNext()) {
			
			final Map<K, TItem<T>> currKeyItems = iterator.next();

			final JsonObject jsonObj = new JsonObject();
			timeDataType.toJsonField(jsonObj, timeFieldName, iterator.getCurrTime());

			for (int j=0; j<keys.size(); j++) {
				
				K key = keys.get(j);

				TItem<T> item = currKeyItems.get(key);
				if (item == null) {
					continue;
				}
				
				String valueFieldName = tsioMap.getFieldName(key);
				TSIOType valueDataType = tsioMap.getDataType(key);
				valueDataType.toJsonField(jsonObj, valueFieldName, item.getObject());
			}

			jsonArr.add(jsonObj);
		}
		return jsonArr;
	}

	public static final <K, T extends Comparable<T>> TFrame<K,T> fromJson(
			final JsonArray jsonArr,
			final String timeFieldName,
			final TSIOType timeDataType, 
			final TSIOMap<K> tsioMap) throws IOException {
		
		TFrame<K, T> frame = new TFrame<>();
		
		if (jsonArr.size() > 0) {
			
			for (int i=0; i<jsonArr.size(); i++) {
				
				JsonObject jsonObj = jsonArr.get(i).getAsJsonObject();
				
				@SuppressWarnings("unchecked")
				T time = (T)timeDataType.fromJsonField(jsonObj, timeFieldName);
				if (time == null) {
					throw new IOException("Data item " + (i+1) + " does not have time field '" + timeFieldName + "'");
				}
				
				for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {

					String fieldName = entry.getKey();
					K key = tsioMap.getKey(fieldName);
					if (key != null) {
						
						TSIOType dataType = tsioMap.getDataType(key);
						Object value = dataType.fromJsonField(jsonObj, fieldName);
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
