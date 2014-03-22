package me.akuz.ts.io.types;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import me.akuz.ts.io.TSIOType;

import org.json.JSONObject;

public final class TSIOTypeDate extends TSIOType {

	private static final ThreadLocal<SimpleDateFormat> _fmt = new ThreadLocal<>();
	
	private final String _format;
	private final TimeZone _timeZone;
	
	public TSIOTypeDate(String format, TimeZone timeZone) {
		_format = format;
		_timeZone = timeZone;
	}

	@Override
	public Object fromJson(JSONObject obj, String name) throws IOException {
		if (!obj.has(name)) {
			return null;
		}
		String str = obj.getString(name);
		SimpleDateFormat fmt = _fmt.get();
		if (fmt == null) {
			fmt = new SimpleDateFormat(_format);
			fmt.setTimeZone(_timeZone);
			_fmt.set(fmt);
		}
		try {
			return fmt.parse(str);
		} catch (ParseException e) {
			throw new IOException("Could not parse date '" + str + "'", e);
		}
	}

	@Override
	public void setJsonField(JSONObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		Date date = (Date)value;
		SimpleDateFormat fmt = _fmt.get();
		if (fmt == null) {
			fmt = new SimpleDateFormat(_format);
			fmt.setTimeZone(_timeZone);
			_fmt.set(fmt);
		}
		obj.put(name, fmt.format(date));
	}
}
