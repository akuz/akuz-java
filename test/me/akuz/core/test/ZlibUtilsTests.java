package me.akuz.core.test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.zip.DataFormatException;

import me.akuz.core.ZlibUtils;

import org.junit.Test;

public final class ZlibUtilsTests {
	
	private static final Charset _encoding = Charset.forName("UTF-8");

	@Test
	public void textDeflateLevel0Inflate() throws UnsupportedEncodingException, DataFormatException {
		
		String strBefore = "Whoever did this, must be a genious!!! Some spaces after...   ";

		byte[] bytes = ZlibUtils.deflate(0, strBefore, _encoding);
		
		String strAfter = ZlibUtils.inflate(bytes, _encoding);
		if (strBefore.equals(strAfter) == false) {
			throw new IllegalStateException("Incorrect compression");
		}
	}

	@Test
	public void textDeflateLevel5Inflate() throws UnsupportedEncodingException, DataFormatException {
		
		String strBefore = "Whoever did this, must be a genious!!! Some spaces after...   ";

		byte[] bytes = ZlibUtils.deflate(5, strBefore, _encoding);
		
		String strAfter = ZlibUtils.inflate(bytes, _encoding);
		if (strBefore.equals(strAfter) == false) {
			throw new IllegalStateException("Incorrect compression");
		}
	}

	@Test
	public void textDeflateLevel9Inflate() throws UnsupportedEncodingException, DataFormatException {
		
		String strBefore = "Whoever did this, must be a genious!!! Some spaces after...   ";

		byte[] bytes = ZlibUtils.deflate(9, strBefore, _encoding);
		
		String strAfter = ZlibUtils.inflate(bytes, _encoding);
		if (strBefore.equals(strAfter) == false) {
			throw new IllegalStateException("Incorrect compression");
		}
	}
}
