package me.akuz.ts.log;

/**
 * Time series operation log alert.
 *
 */
public final class TLogAlert {
	
	private final TLogLevel _level;
	private final String _message;
	
	public TLogAlert(final TLogLevel level, final String message) {
		_level = level;
		_message = message;
	}
	
	public TLogLevel getLevel() {
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
