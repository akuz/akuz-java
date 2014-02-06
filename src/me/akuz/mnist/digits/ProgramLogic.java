package me.akuz.mnist.digits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import me.akuz.core.FileUtils;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;

public final class ProgramLogic {
	
	private final int IMAGE_SIZE = 28;
	
	public ProgramLogic() {
	}

	public void execute(Monitor parentMonitor, ProgramOptions options) throws Exception {
		
		final LocalMonitor monitor = new LocalMonitor(this.getClass().getSimpleName(), parentMonitor);
		
		monitor.write("Checking output dir...");
		if (!FileUtils.isDirExistsOrCreate(options.getOutputDir())) {
			throw new IOException("Could not create output dir: " + options.getOutputDir());
		}
		
		monitor.write("Loading training data...");
		List<Digit> digits = new ArrayList<>();
		try (Scanner scanner = FileUtils.openScanner(options.getTrainFile())) {
			
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
					byte symbol = Byte.parseByte(parts[0]);
					byte[][] data = new byte[IMAGE_SIZE][IMAGE_SIZE];
					for (int i=1; i<parts.length; i++) {
						
						int val = Integer.parseInt(parts[i]);
						final int index = i-1;
						final int row = index / IMAGE_SIZE;
						final int col = index % IMAGE_SIZE;
						data[row][col] = (byte)val;
					}
					Digit digit = new Digit(symbol, data);
					digits.add(digit);
				}
				counter += 1;
			}
		}
		
		System.gc();
		System.out.println("Press any key...");
		System.in.read();
		
		monitor.write("DONE.");
	}
}
