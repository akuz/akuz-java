package me.akuz.core;

import me.akuz.core.UrlUtils;

import org.junit.Test;

public final class UrlUtilsTest {

	@Test
	public void testUrlUtilsNormalization0() {
		String url = UrlUtils.absolutizeUrl("http://www.readrz.com", "test.html");
		if (!url.equals("http://www.readrz.com/test.html")) {
			throw new IllegalStateException("Incorrect URL normalization");
		}
	}

	@Test
	public void testUrlUtilsNormalization1() {
		String url = UrlUtils.absolutizeUrl("http://www.readrz.com/", "/test.html");
		if (!url.equals("http://www.readrz.com/test.html")) {
			throw new IllegalStateException("Incorrect URL normalization");
		}
	}

	@Test
	public void testUrlUtilsNormalization2() {
		String url = UrlUtils.absolutizeUrl("http://www.readrz.com/here/we/go", "/test.html");
		if (!url.equals("http://www.readrz.com/test.html")) {
			throw new IllegalStateException("Incorrect URL normalization");
		}
	}

	@Test
	public void testUrlUtilsNormalization3() {
		String url = UrlUtils.absolutizeUrl("http://www.readrz.com/here/we/go", "test.html");
		if (!url.equals("http://www.readrz.com/here/we/test.html")) {
			throw new IllegalStateException("Incorrect URL normalization");
		}
	}

	@Test
	public void testUrlUtilsNormalization4() {
		String url = UrlUtils.absolutizeUrl("http://www.readrz.com/here/we/go/", "test.html");
		if (!url.equals("http://www.readrz.com/here/we/go/test.html")) {
			throw new IllegalStateException("Incorrect URL normalization");
		}
	}
	
}
