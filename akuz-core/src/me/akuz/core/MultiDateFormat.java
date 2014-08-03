package me.akuz.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class MultiDateFormat {
	
	private volatile boolean _isThreadSafe;
	private final List<SimpleDateFormat> _formats;
	
	public MultiDateFormat() {
		_formats = new ArrayList<>();
	}
	
	public boolean isThreadSafe() {
		return _isThreadSafe;
	}
	public void isThreadSafe(boolean is) {
		_isThreadSafe = is;
	}
	
	public void addFormat(String formatString) {
		SimpleDateFormat sdf = new SimpleDateFormat(formatString);
		sdf.setCalendar(DateUtils.getUTCCalendar());
		_formats.add(sdf);
	}
	
	public Date parse(String str) throws ParseException {
		if (_isThreadSafe) {
			synchronized (_formats) {
				return parseUnsafe(str);
			}
		} else {
			return parseUnsafe(str);
		}
	}
	
	private Date parseUnsafe(String str) throws ParseException {
		
		// fix the Java time zone issue with colon
		str = str.replaceAll("([\\+\\-]\\d\\d):(\\d\\d)","$1$2");
		
		Date result = null;
		for (int i=0; i<_formats.size(); i++) {
			SimpleDateFormat sdf = _formats.get(i);
			try {
				result = sdf.parse(str);
				if (result != null) {
					break;
				}
			} catch (ParseException e) {
				// could not parse
			}
		}
		if (result == null) {
			if (_formats.size() == 0) {
				throw new IllegalStateException("Could not parse date because possible formats not specified");
			} else {
				throw new ParseException("Could not parse date: " + str, 0);
			}
		}
		return result;
	}

}
