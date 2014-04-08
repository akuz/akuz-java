package me.akuz.core;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class ZlibUtils {
	
	private static final int MAX_THREAD_BUFFERS = 100;
	private static final int THREAD_BUFFER_LENGTH = 1000;
	private static final Object _lock = new Object();
	private static final Map<Long, byte[]> _threadBuffers = new HashMap<>();
	private static final Queue<Long> _threadQueue = new LinkedList<>();
	
	private static final byte[] getThreadBuffer() {
		
		byte[] buffer;
		synchronized (_lock) {
			
			// get thread buffer
			Long threadId = Thread.currentThread().getId();
			buffer = _threadBuffers.get(threadId);
			if (buffer == null) {
				buffer = new byte[THREAD_BUFFER_LENGTH];
				_threadBuffers.put(threadId, buffer);
				_threadQueue.add(threadId);
			}
			
			// throw away old buffers
			while (_threadQueue.size() > MAX_THREAD_BUFFERS) {
				Long removeThreadId = _threadQueue.poll();
				_threadBuffers.remove(removeThreadId);
			}
		}
		
		return buffer;
	}
	
	public static final byte[] deflate(int level, String str, Charset encoding) throws UnsupportedEncodingException {
		
		// string to bytes
		byte[] bytes = str.getBytes(encoding);

		// deflate bytes
		Deflater def = new Deflater(level, true);
		def.reset();
		def.setInput(bytes);
		def.finish();

		// get thread buffer
		byte[] buffer = getThreadBuffer();
		
		// write deflated output
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (!def.finished()) {
			int n = def.deflate(buffer);
			baos.write(buffer, 0, n);
		}
		baos.write(new byte[1], 0, 1);
		
		// get and return result
		byte[] result = baos.toByteArray();
		return result;
	}
	
	public static final String inflate(byte[] bytes, Charset encoding) throws DataFormatException, UnsupportedEncodingException {

		// create inflater
		Inflater inf = new Inflater(true);
		inf.reset();
		inf.setInput(bytes);

		// get thread buffer
		byte[] buffer = getThreadBuffer();

		// inflate and write output
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (!inf.finished()) {
			int n = inf.inflate(buffer);
			baos.write(buffer, 0, n);
		}
		
		// get result and return
		byte[] bytes2 = baos.toByteArray();
		String str = new String(bytes2, encoding);
		return str;
	}

}
