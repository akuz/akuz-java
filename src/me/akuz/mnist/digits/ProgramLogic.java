package me.akuz.mnist.digits;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import me.akuz.core.FileUtils;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;

public final class ProgramLogic {

	private static final int IMAGE_SIZE = 28;
	private static final int MAX_IMAGE_COUNT = 200;
	
	private static final int DIM2  = 12;
	private static final int ITER2 = 2;
	
	private static final int DIM4  = 16;
	private static final int ITER4 = 2;
	
	private static final int DIM8  = 24;
	private static final int ITER8 = 2;
	
	private static final int DIM16  = 40;
	private static final int ITER16 = 2;
	
	private static final double LOG_LIKE_CHANGE_THRESHOLD = 0.001;
	private static final DecimalFormat fmt = new DecimalFormat("0.00000000");
	
	public ProgramLogic() {
	}

	public void execute(Monitor parentMonitor, ProgramOptions options) throws Exception {
		
		final LocalMonitor monitor = new LocalMonitor(this.getClass().getSimpleName(), parentMonitor);
		
		monitor.write("Checking output dir...");
		if (!FileUtils.isDirExistsOrCreate(options.getOutputDir())) {
			throw new IOException("Could not create output dir: " + options.getOutputDir());
		}
		
		monitor.write("Loading training data...");
		List<ByteImage> images = new ArrayList<>();
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
					ByteImage digit = new ByteImage(symbol, data);
					images.add(digit);
				}
				counter += 1;
				
				if (images.size() >= MAX_IMAGE_COUNT) {
					break;
				}
			}
		}
		
		monitor.write("Interring 2x2 blocks...");
		InferNKP infer2x2 = new InferNKP(monitor, images, DIM2, 1);
		infer2x2.execute(monitor, ITER2, LOG_LIKE_CHANGE_THRESHOLD);
		
		monitor.write("Cleaning output dir...");
		FileUtils.cleanDir(options.getOutputDir());
		
		monitor.write("Saving 2x2 features...");
		SaveBlocks.save2x2(
				infer2x2.getFeatureProbs(),
				infer2x2.getFeatureBlocks(),
				options.getOutputDir());
		
		monitor.write("Interring 4x4 blocks...");
		InferHDP infer4x4 = new InferHDP(infer2x2.getFeatureImages(), DIM2, DIM4, 2);
		infer4x4.execute(monitor, ITER4, LOG_LIKE_CHANGE_THRESHOLD);
		
		monitor.write("Saving 4x4 features...");
		SaveBlocks.save4x4(
				infer4x4.getFeatureProbs(),
				infer4x4.getFeatureBlocks(), 
				infer2x2.getFeatureBlocks(), 
				options.getOutputDir());
		
		monitor.write("Interring 2x2 blocks (loop 2)...");
		infer2x2.setParentFeatureImages(infer4x4.getFeatureShift(), infer4x4.getFeatureBlocks(), infer4x4.getFeatureImages());
		infer2x2.execute(monitor, ITER2, LOG_LIKE_CHANGE_THRESHOLD);
		
		monitor.write("Saving 2x2 features (loop 2)...");
		SaveBlocks.save2x2(
				infer2x2.getFeatureProbs(),
				infer2x2.getFeatureBlocks(), 
				options.getOutputDir());

		monitor.write("Interring 4x4 blocks (loop 2)...");
		infer4x4.execute(monitor, ITER4, LOG_LIKE_CHANGE_THRESHOLD);
		
		monitor.write("Saving 4x4 features (loop 2)...");
		SaveBlocks.save4x4(
				infer4x4.getFeatureProbs(), 
				infer4x4.getFeatureBlocks(), 
				infer2x2.getFeatureBlocks(), 
				options.getOutputDir());

		monitor.write("Interring 8x8 blocks...");
		InferHDP infer8x8 = new InferHDP(infer4x4.getFeatureImages(), DIM4, DIM8, 4);
		infer8x8.execute(monitor, ITER8, LOG_LIKE_CHANGE_THRESHOLD);
		
		monitor.write("Saving 8x8 features...");
		SaveBlocks.save8x8(
				infer8x8.getFeatureProbs(), 
				infer8x8.getFeatureBlocks(), 
				infer4x4.getFeatureBlocks(), 
				infer2x2.getFeatureBlocks(),
				options.getOutputDir());

		monitor.write("Interring 16x16 blocks...");
		InferHDP infer16x16 = new InferHDP(infer8x8.getFeatureImages(), DIM8, DIM16, 8);
		infer16x16.execute(monitor, ITER16, LOG_LIKE_CHANGE_THRESHOLD);
		
		monitor.write("Saving 16x16 features...");
		SaveBlocks.save16x16(
				infer16x16.getFeatureProbs(), 
				infer16x16.getFeatureBlocks(), 
				infer8x8.getFeatureBlocks(), 
				infer4x4.getFeatureBlocks(), 
				infer2x2.getFeatureBlocks(), 
				options.getOutputDir());
		
//		monitor.write("Press any key to exit...");
//		System.in.read();
		
		monitor.write("DONE.");
	}
}
