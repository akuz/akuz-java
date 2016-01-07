package me.akuz.mnist.digits.load;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import me.akuz.core.FileUtils;
import me.akuz.core.geom.BWImage;

public final class MNIST {
	
	private static final int IMAGE_WIDTH = 28;
	private static final int IMAGE_HEIGHT = 28;
	private static final int IMAGE_SIZE = IMAGE_WIDTH*IMAGE_HEIGHT;
	
	public static void load_train (
			final String fileName,
			final List<Integer> digits,
			final List<BWImage> images,
			final int maxCount) throws IOException {
		
		load(fileName, true, digits, images, maxCount);
	}
	
	public static void load_test (
			final String fileName,
			final List<BWImage> images,
			final int maxCount) throws IOException {
		
		load(fileName, false, null, images, maxCount);
	}
	
	private static void load (
			final String fileName,
			final boolean isTraining,
			final List<Integer> digits,
			final List<BWImage> images,
			final int maxCount) throws IOException {
		
		try (Scanner scanner = FileUtils.openScanner(fileName)) {
			
			// skip first line
			if (scanner.hasNextLine()) {
				scanner.nextLine();
			}

			// load all other lines
			int lineNumber = 2;
			int loadedCount = 0;
			final int requiredEntryCount = isTraining 
					? IMAGE_SIZE + 1
					: IMAGE_SIZE;

			while (scanner.hasNextLine()) {
				
				String line = scanner.nextLine().trim();
				if (line.length() > 0) {

					String[] parts = line.split(",");
					if (parts.length != requiredEntryCount) {
						throw new IOException("Incorrect number of entries in line #" + lineNumber + ": " + line);
					}

					int startIndex = 0;
					if (isTraining) {
						int digit = Integer.parseInt(parts[0]);
						digits.add(digit);
						startIndex = 1;
					}
					
					BWImage image = new BWImage(IMAGE_HEIGHT, IMAGE_WIDTH);
					for (int i=0; i<IMAGE_SIZE; i++) {

						final int row = i / IMAGE_WIDTH;
						final int col = i % IMAGE_WIDTH;
						int val = Integer.parseInt(parts[startIndex + i]);
						image.setColor(row, col, val / 255.0);
					}
					
					images.add(image);
					loadedCount += 1;
				}
				
				if (loadedCount >= maxCount) {
					break;
				}
				
				lineNumber += 1;
			}
		}
	}
	
}
