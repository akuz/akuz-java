package me.akuz.ts.io;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.akuz.core.Out;
import me.akuz.ts.Frame;
import me.akuz.ts.FrameIterator;
import me.akuz.ts.TItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * JSON time series IO functions.
 *
 */
public final class JSON_IO {

	/**
	 * Serialize a frame according to provided map.
	 */
	public static final <K, T extends Comparable<T>> JsonArray toJson(
			final Frame<K, T> frame,
			final IOMap<K> ioMap) {
		
		final JsonArray jsonArr = new JsonArray();
		
		final List<K> keys = ioMap.getKeys();

		final FrameIterator<K, T> frameIter = new FrameIterator<K, T>(frame, keys);
		final Out<T> nextTime = new Out<>();
		while (frameIter.getNextTime(nextTime)) {
			
			frameIter.moveToTime(nextTime.getValue());
			
			final Map<K, TItem<T>> currKeyItems = frameIter.getCurrItems();

			final JsonObject jsonObj = new JsonObject();
			
			ioMap.getTimeDataType().toJsonField(
					jsonObj, 
					ioMap.getTimeFieldName(), 
					frameIter.getCurrTime());

			for (int j=0; j<keys.size(); j++) {
				
				final K key = keys.get(j);

				final TItem<T> item = currKeyItems.get(key);
				if (item == null) {
					continue;
				}
				
				ioMap.getDataType(key).toJsonField(
						jsonObj, 
						ioMap.getFieldName(key), 
						item.getObject());
			}

			jsonArr.add(jsonObj);
		}
		return jsonArr;
	}

	public static final <K, T extends Comparable<T>> Frame<K,T> fromJson(
			final JsonArray jsonArr,
			final IOMap<K> ioMap) throws IOException {
		
		Frame<K, T> frame = new Frame<>();
		
		if (jsonArr.size() > 0) {
			
			for (int i=0; i<jsonArr.size(); i++) {
				
				JsonObject jsonObj = jsonArr.get(i).getAsJsonObject();
				
				@SuppressWarnings("unchecked")
				T time = (T)ioMap.getTimeDataType().fromJsonField(
						jsonObj, 
						ioMap.getTimeFieldName());
				
				if (time == null) {
					throw new IOException("Data item #" + (i+1) + " does not have a time field '" + ioMap.getTimeFieldName() + "'");
				}
				
				for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {

					final String fieldName = entry.getKey();
					final K key = ioMap.getKey(fieldName);
					if (key != null) {

						final Object value = ioMap.getDataType(key).fromJsonField(jsonObj, fieldName);
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
