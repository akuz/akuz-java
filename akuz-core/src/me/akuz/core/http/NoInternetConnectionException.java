package me.akuz.core.http;

/**
 * Thrown when there is no Internet connection available.
 * @author andrey
 *
 */
public final class NoInternetConnectionException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final String _diagnosticMessage;
	
	public NoInternetConnectionException(String diagnosticMessage) {
		_diagnosticMessage = diagnosticMessage;
	}
	
	public String getDiagnosticMessage() {
		return _diagnosticMessage;
	}
	
	public String getMessage() {
		return String.format("There is no Internet connection (%s)", _diagnosticMessage);
	}
	
	public String toString() {
		return String.format("%s: %s", getClass().getSimpleName(), getMessage());
	}
}
