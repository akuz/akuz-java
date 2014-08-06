package me.akuz.ts.log;

/**
 * Time series log level.
 *
 */
public final class TLogLevel implements Comparable<TLogLevel> {

	public static final TLogLevel None    = new TLogLevel(0, "None");
	public static final TLogLevel Info    = new TLogLevel(1, "Info");
	public static final TLogLevel Warning = new TLogLevel(2, "Warning");
	public static final TLogLevel Error   = new TLogLevel(3, "Error");
	
	private final int _code;
	private final String _name;
	
	private TLogLevel(final int code, final String name) {
		_code = code;
		_name = name;
	}
	
	public int getCode() {
		return _code;
	}
	
	public String getName() {
		return _name;
	}

	@Override
	public int compareTo(TLogLevel o) {
		return _code - o._code;
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
	@Override
	public int hashCode() {
		return _code;
	}
}
