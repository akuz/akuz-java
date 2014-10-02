package me.akuz.ts.log;

/**
 * Time series logging level.
 *
 */
public final class TLevel implements Comparable<TLevel> {

	public static final TLevel None     = new TLevel(0, "None");
	public static final TLevel Info     = new TLevel(1, "Info");
	public static final TLevel Warning  = new TLevel(2, "Warning");
	public static final TLevel Danger   = new TLevel(3, "DANGER");
	
	private final int _code;
	private final String _name;
	
	private TLevel(final int code, final String name) {
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
	public int compareTo(TLevel o) {
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
