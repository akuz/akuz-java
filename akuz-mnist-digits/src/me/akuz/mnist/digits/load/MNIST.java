package me.akuz.mnist.digits.load;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import me.akuz.core.FileUtils;
import me.akuz.core.geom.ByteImage;

public final class MNIST {
	
	private static final int IMAGE_SIZE = 28;

	private final List<Integer> _digits;
	private final List<ByteImage> _images;
	
	public MNIST(
			List<Integer> digits,
			List<ByteImage> images) {
		
		if (digits == null) {
			throw new NullPointerException("digits");
		}
		if (images == null) {
			throw new NullPointerException("images");
		}
		if (digits.size() != images.size()) {
			throw new IllegalArgumentException(
					"Digits size is " + digits.size() + 
					", but images size is " + images.size());
		}
		_digits = digits;
		_images = images;
	}
	
	public List<Integer> getDigits() {
		return _digits;
	}
	
	public List<ByteImage> getImages() {
		return _images;
	}
	
	public static MNIST load(
			final String fileName,
			final int maxImageCount) throws IOException {
		
		List<Integer> digits = new ArrayList<>();
		List<ByteImage> images = new ArrayList<>();
		try (Scanner scanner = FileUtils.openScanner(fileName)) {
			
			// skip first line
			if (scanner.hasNextLine()) {
				scanner.nextLine();
			}

			// load all other lines
			int counter = 2;
			final int requiredEntryCount = 1 + IMAGE_SIZE*IMAGE_SIZE;
			while (scanner.hasNextLine()) {
				
				String line = scanner.nextLine().trim();
				if (line.length() > 0) {
					String[] parts = line.split(",");
					if (parts.length != requiredEntryCount) {
						throw new IOException("Incorrect number of entries in line #" + counter + ": " + line);
					}
					int number = Integer.parseInt(parts[0]);
					digits.add(number);
					byte[][] data = new byte[IMAGE_SIZE][IMAGE_SIZE];
					for (int i=1; i<parts.length; i++) {
						
						int val = Integer.parseInt(parts[i]);
						final int index = i-1;
						final int row = index / IMAGE_SIZE;
						final int col = index % IMAGE_SIZE;
						data[row][col] = (byte)val;
					}
					ByteImage digit = new ByteImage(data);
					images.add(digit);
				}
				counter += 1;
				
				if (images.size() >= maxImageCount) {
					break;
				}
			}
		}
		
		return new MNIST(digits, images);
	}
	
}
