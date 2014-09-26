package me.akuz.ts.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import me.akuz.core.FileUtils;
import me.akuz.core.Out;
import me.akuz.ts.Frame;
import me.akuz.ts.FrameIterator;
import me.akuz.ts.TItem;

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

	public static final <K, T extends Comparable<T>> void toCSV(
			final File file,
			final Frame<K, T> frame, 
			final String timeField,
			final IOType timeType,
			final IOType dataType) throws IOException {
		
		toCSV(
			file,
			frame,
			new IOMap<>(
					timeField, 
					timeType,
					frame,
					dataType));
	}

	public static final <K, T extends Comparable<T>> String toCSV(
			final Frame<K, T> frame, 
			final String timeField,
			final IOType timeType,
			final IOType dataType) {
		
		return toCSV(
				frame,
				new IOMap<>(
						timeField, 
						timeType,
						frame,
						dataType));
	}

	public static final <K, T extends Comparable<T>> void toCSV(
			final File file,
			final Frame<K, T> frame,
			final IOMap<K> ioMap) throws IOException {
		
		final FileOutputStream output = new FileOutputStream(file, false);
		final OutputStreamWriter outputWriter = new OutputStreamWriter(output, FileUtils.UTF8);
		try (final BufferedWriter bufferedWriter = new BufferedWriter(outputWriter)) {
			toCSV(bufferedWriter, frame, ioMap);
		}
	}

	public static final <K, T extends Comparable<T>> String toCSV(
			final Frame<K, T> frame,
			final IOMap<K> ioMap) {
		
		try {
			try (final StringWriter writer = new StringWriter()) {
				toCSV(writer, frame, ioMap);
				return writer.getBuffer().toString();
			}
		} catch (IOException e) {
			throw new IllegalStateException("Could not write to a string", e);
		}
	}

	public static final <K, T extends Comparable<T>> void toCSV(
			final Writer writer,
			final Frame<K, T> frame,
			final IOMap<K> ioMap) throws IOException {

		writer.append(escape(ioMap.getTimeFieldName()));

		List<K> keys = ioMap.getKeys();
		for (int j=0; j<keys.size(); j++) {
			
			writer.append(SEP);
			K key = keys.get(j);
			writer.append(escape(ioMap.getFieldName(key)));
		}
		writer.append(NEW_LINE);
		
		final FrameIterator<K, T> frameIter = new FrameIterator<>(frame, keys);
		final Out<T> nextTime = new Out<>();
		while (frameIter.getNextTime(nextTime)) {
			
			frameIter.moveToTime(nextTime.getValue());
			
			final Map<K, TItem<T>> currKeyItems = frameIter.getCurrItems();
			
			writer.append(escape(ioMap.getTimeDataType().toString(frameIter.getCurrTime())));
			for (int j=0; j<keys.size(); j++) {
				
				writer.append(SEP);
				K key = keys.get(j);
				TItem<T> item = currKeyItems.get(key);
				if (item != null) {
					writer.append(escape(ioMap.getDataType(key).toString(item.getObject())));
				}
			}
			writer.append(NEW_LINE);
		}
	}

	public static final <K, T extends Comparable<T>> Frame<K,T> fromCSV(
			final String data,
			final IOMap<K> ioMap) {
		
		try {
			return fromCSV(new StringReader(data), ioMap);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read from a string", e);
		}
	}

	public static final <K, T extends Comparable<T>> Frame<K,T> fromCSV(
			final File file,
			final IOMap<K> ioMap) throws IOException {
		
		final FileInputStream input = new FileInputStream(file);
		try (final InputStreamReader reader = new InputStreamReader(input, FileUtils.UTF8))
		{
			return fromCSV(reader, ioMap);
		}
	}

	public static final <K, T extends Comparable<T>> Frame<K,T> fromCSV(
			final Readable input,
			final IOMap<K> ioMap) throws IOException {
		
		Frame<K, T> frame = new Frame<>();

		int lineIndex = 0;
		try (final Scanner scanner = new Scanner(input)) {
			
			int timeFieldIdx = -1;
			Map<Integer, K> fieldIdxToKey = null;
			int fieldCount = -1;
			
			while (scanner.hasNextLine()) {
				
				String line = scanner.nextLine();
				
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
