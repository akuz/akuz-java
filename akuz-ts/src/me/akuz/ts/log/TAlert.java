package me.akuz.ts.log;

/**
 * Time series logging alert.
 *
 */
public final class TAlert {
	
	private final TLevel _level;
	private final String _message;
	
	public TAlert(final TLevel level, final String message) {
		_level = level;
		_message = message;
	}
	
	public TLevel getLevel() {
		return _level;
	}
	
	public String getMessage() {
		return _message;
	}
	
	@Override
	public String toString() {
		return _level + ": " + _message;
	}

}
