package me.akuz.core.http;

import java.io.IOException;

/**
 * Thrown when HTTP response code is not 200.
 * @author andrey
 *
 */
public final class Non200HttpCodeExeption extends IOException {

	private static final long serialVersionUID = 1L;
	
	private final int _httpCode;
	private final String _url;
	
	public Non200HttpCodeExeption(int httpCode, String url) {
		_httpCode = httpCode;
		_url = url;
	}
	
	public int getHttpCode() {
		return _httpCode;
	}
	
	public String getMessage() {
		return String.format("HTTP response code %d when getting URL: %s", _httpCode, _url);
	}
	
	public String toString() {
		return String.format("%s: %s", getClass().getSimpleName(), getMessage());
	}
}
