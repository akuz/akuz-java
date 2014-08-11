package me.akuz.ts.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import me.akuz.core.FileUtils;
import me.akuz.ts.Frame;
import me.akuz.ts.TItem;
import me.akuz.ts.filters.FrameWalkerOld;

/**
 * CSV time series IO functions.
 *
 */
public final class CSV_IO {
	
	private static final String SEP = ",";
	private static final String SEP_REPLACEMENT = "\\\\,";
	private static final String SEP_REGEX = ",";
	private static final String SEP_REGEX_UNESCAPED = "(?<=(^|[^\\\\])),";
	private static final String SEP_REGEX_ESCAPED = "\\\\,";
	private static final String NEW_LINE = "\n";
	
	private static final String escape(final String str) {
		return str.replaceAll(SEP_REGEX, SEP_REPLACEMENT);
	}
	
	private static final String unescape(final String str) {
		return str.replaceAll(SEP_REGEX_ESCAPED, SEP);
	}
	
	private static final String[] split(final String str) {
		return str.split(SEP_REGEX_UNESCAPED);
	}

	public static final <K, T extends Comparable<T>> void saveCSV(
			final String fileName,
			final Frame<K, T> frame,
			final IOMap<K> ioMap) throws IOException {
		
		final String str = toCSV(frame, ioMap);
		FileUtils.writeEntireFile(fileName, str);
	}

	public static final <K, T extends Comparable<T>> String toCSV(
			final Frame<K, T> frame,
			final IOMap<K> ioMap) {
		
		final StringBuilder sb = new StringBuilder();
		sb.append(escape(ioMap.getTimeFieldName()));

		List<K> keys = ioMap.getKeys();
		for (int j=0; j<keys.size(); j++) {
			
			sb.append(SEP);
			K key = keys.get(j);
			sb.append(escape(ioMap.getFieldName(key)));
		}
		sb.append(NEW_LINE);
		
		final Set<T> timeSet = new HashSet<>();
		frame.extractTimes(timeSet);

		final FrameWalkerOld<K, T> frameAligner = new FrameWalkerOld<>(frame, keys, timeSet);
		while (frameAligner.hasNext()) {
			
			frameAligner.next();
			
			final Map<K, TItem<T>> currKeyItems = frameAligner.getCurrItems();
			
			sb.append(escape(ioMap.getTimeDataType().toString(frameAligner.getCurrTime())));
			for (int j=0; j<keys.size(); j++) {
				
				sb.append(SEP);
				K key = keys.get(j);
				TItem<T> item = currKeyItems.get(key);
				if (item != null) {
					sb.append(escape(ioMap.getDataType(key).toString(item.getObject())));
				}
			}
			sb.append(NEW_LINE);
		}
		return sb.toString();
	}

	public static final <K, T extends Comparable<T>> Frame<K,T> fromCSV(
			final String data,
			final IOMap<K> ioMap) throws IOException {
		
		Frame<K, T> frame = new Frame<>();

		int lineIndex = 0;
		try (Scanner sc = new Scanner(data)) {
			
			int timeFieldIdx = -1;
			Map<Integer, K> fieldIdxToKey = null;
			int fieldCount = -1;
			
			while (sc.hasNextLine()) {
				
				String line = sc.nextLine();
				
				if (lineIndex == 0) {
					
					// read the headers
					final String[] parts = split(line);
					fieldCount = parts.length;
					fieldIdxToKey = new HashMap<>();
					for (int i=0; i<parts.length; i++) {
						final String fieldName = unescape(parts[i].trim());
						if (fieldName.equalsIgnoreCase(ioMap.getTimeFieldName())) {
							timeFieldIdx = i;
						} else {
							K key = ioMap.getKey(fieldName);
							if (key != null) {
								fieldIdxToKey.put(i, key);
							}
						}
					}
					if (timeFieldIdx < 0) {
						throw new IOException("Could not find time field '" + ioMap.getTimeFieldName() + "' in the headers");
					}
					
				} else {
					
					// read the data line
					final String[] parts = split(line);
					if (parts.length != fieldCount) {
						throw new IOException(
								"Number of fields (" + parts.length + ") at line index " + lineIndex + 
								" does not match the number of fields (" + fieldCount + ") in the headers");
					}
					
					final String timeStr = unescape(parts[timeFieldIdx].trim());
					final T time;
					try {
						@SuppressWarnings("unchecked")
						final T parsedTime = (T)ioMap.getTimeDataType().fromString(timeStr);
						time = parsedTime;
					} catch (Exception ex) {
						throw new IOException("Could not parse time at line index " + lineIndex + ": '" + timeStr + "'");
					}
					
					for (Integer fieldIdx : fieldIdxToKey.keySet()) {
						
						final String partStr = unescape(parts[fieldIdx].trim());
						if (partStr.length() == 0) {
							continue;
						}
						final K key = fieldIdxToKey.get(fieldIdx);
						final Object value = ioMap.getDataType(key).fromString(partStr);
						if (value != null) {
							frame.stage(key, time, value);
						}
					}
				}
				
				lineIndex++;
			}
		}
		
		frame.acceptStaged();
		return frame;
	}
}
