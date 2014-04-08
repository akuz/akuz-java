package me.akuz.core.http;

import java.io.IOException;

public final class NonHttpProtocolException extends IOException {

	private static final long serialVersionUID = 1L;
	
	private final String _connectionType;
	
	public NonHttpProtocolException(String connectionType) {
		_connectionType = connectionType;
	}
	
	public final String getConnectionType() {
		return _connectionType;
	}
	
	public final String getMessage() {
		return String.format("Non-HTTP protocol (connected as %s)", _connectionType);
	}
	
	public final String toString() {
		return String.format("%s: %s", getClass().getSimpleName(), getMessage());
	}

}
