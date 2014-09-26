package me.akuz.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Various file access utilities.
 *
 */
public final class FileUtils {
	
	public final static String UTF8 =  "UTF-8";
	
	/**
	 * Returns a single file object if the argument is a path to a specific file,
	 * or a list of directory files, if the argument is a path to a folder.
	 */
	public static final List<File> getFiles(String path) {
		File file = new File(path);
		List<File> list = new ArrayList<File>();
		if (file.exists()) {
			if (file.isFile()) {
				list.add(file);
			} else if (file.isDirectory()) {
				File[] files = file.listFiles();
				if (files != null) {
					for (int i=0; i<files.length; i++) {
						File f = files[i];
						if (f.isFile()) {
							list.add(f);
						}
					}
				}
			}
		}
		return list;
	}
	
	public static final Scanner openScanner(File file) throws IOException {
		return openScanner(file, null);
	}
	public static final Scanner openScanner(String fileName) throws IOException {
		return openScanner(new File(fileName), null);
	}
	public static final Scanner openScanner(String fileName, String encoding) throws IOException {
		return openScanner(new File(fileName), encoding);
	}
	public static final Scanner openScanner(File file, String encoding) throws IOException {
		if (encoding == null) {
			encoding = UTF8;
		}
		FileInputStream fis = new FileInputStream(file);
		return new Scanner(fis, encoding);
	}
	
	public static final String readEntireFile(File file) throws IOException {
		return readEntireFile(file, null);
	}
	public static final String readEntireFile(String fileName) throws IOException {
		return readEntireFile(new File(fileName), null);
	}
	public static final String readEntireFile(String fileName, String encoding) throws IOException {
		return readEntireFile(new File(fileName), encoding);
	}
	public static final String readEntireFile(File file, String encoding) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = openScanner(file, encoding)) {
			String NL = System.getProperty("line.separator");
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine());
				sb.append(NL);
			}
		}
		return sb.toString();
	}

	public static final List<String> readEntireFileLines(File file) throws IOException {
		return readEntireFileLines(file, null);
	}
	public static final List<String> readEntireFileLines(String fileName) throws IOException {
		return readEntireFileLines(new File(fileName), null);
	}
	public static final List<String> readEntireFileLines(String fileName, String encoding) throws IOException {
		return readEntireFileLines(new File(fileName), encoding);
	}
	public static final List<String> readEntireFileLines(File file, String encoding) throws IOException {
		List<String> lines = new ArrayList<String>();
		try (Scanner scanner = openScanner(file, encoding)) {
			while(scanner.hasNextLine()) {
				lines.add(scanner.nextLine());
			}
		}
		return lines;
	}

	public static List<String> readLinesNoComments(String fileName) throws IOException {
		return readLinesNoComments(fileName, null);
	}
	public static List<String> readLinesNoComments(String fileName, String encoding) throws IOException {
		List<String> lines = new ArrayList<>();
		try (Scanner scanner = openScanner(fileName, encoding)) {
	        while (scanner.hasNextLine()) {
	        	String line = scanner.nextLine().trim();
	        	if (line.length() > 0) {
		        	if (line.startsWith("#") == false) {
		        		lines.add(line);
		        	}
	        	}
	        }
	    }
		return lines;
	}

	public static final void writeEntireFile(String fileName, String contents) throws IOException {
		writeEntireFile(new File(fileName), contents, null);
	}
	public static final void writeEntireFile(String fileName, String contents, String encoding) throws IOException {
		writeEntireFile(new File(fileName), contents, encoding);
	}
	public static final void writeEntireFile(File file, String contents) throws IOException {
		writeEntireFile(file, contents, null);
	}
	public static final void writeEntireFile(File file, String contents, String encoding) throws IOException {
		if (encoding == null) {
			encoding = UTF8;
		}
		try (	FileOutputStream fos = new FileOutputStream(file, false);
				Writer writer = new OutputStreamWriter(fos, encoding)) {

			writer.write(contents);
		}
	}

	public static final void writeList(String fileName, List<?> list) throws IOException {
		writeList(fileName, list, null);
	}
	public static final void writeList(String fileName, List<?> list, String encoding) throws IOException {
		if (encoding == null) {
			encoding = UTF8;
		}
		try (	FileOutputStream fos = new FileOutputStream(fileName);
				Writer writer = new OutputStreamWriter(fos, encoding)) {
			
			String NL = System.getProperty("line.separator");
			for (int i=0; i<list.size(); i++) {
				String lineText = "" + list.get(i);
				writer.write(lineText);
				writer.write(NL);
			}
		}
	}
	
	public static final byte[] readFileBytes(String fileName) throws IOException {
		
		File file = new File(fileName);

	    // Get the size of the file
	    long length = file.length();

	    // You cannot create an array using a long type.
	    // It needs to be an int type.
	    // Before converting to an int type, check
	    // to ensure that file is not larger than Integer.MAX_VALUE.
	    if (length > Integer.MAX_VALUE) {
	        throw new IOException("File " + fileName + " is too large");
	    }

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
		try (InputStream is = new FileInputStream(fileName)) {
		    while (offset < bytes.length && 
		    	(numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		        offset += numRead;
		    }
	
		    // Ensure all the bytes have been read in
		    if (offset < bytes.length) {
		        throw new IOException("Could not completely read file " + fileName);
		    }
		}
	    return bytes;		
	}

	public static boolean isFileExists(String path) {
		File f = new File(path);
		return f.isFile() && f.exists();
	}

	public static boolean isDirExists(String path) {
		File f = new File(path);
		return f.isDirectory() && f.exists();
	}
	
	public static final boolean isDirExistsOrCreate(String path) {
		if (isDirExists(path) == false) {
			File f = new File(path);
			return f.mkdirs();
		} else {
			return true;
		}
	}

	public static void cleanDir(String path) throws IOException {
		
		File dir = new File(path);
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new IOException("Path is not a directory: " + path);
			}
			for(File file: dir.listFiles()) {
				file.delete();
			}
		}
		
	}
}
