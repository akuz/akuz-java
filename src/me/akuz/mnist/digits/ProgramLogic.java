package me.akuz.mnist.digits;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import me.akuz.core.FileUtils;
import me.akuz.core.StringUtils;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.math.NKPDist;

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
				
				if (counter >= 5000) {
					break;
				}
			}
		}
		
		monitor.write("Interring 2x2 blocks...");
		Infer2x2 infer2x2 = new Infer2x2(digits, 8);
		
		monitor.write("Cleaning output dir...");
		FileUtils.cleanDir(options.getOutputDir());
		
		monitor.write("Saving results to output dir...");
		NKPDist[][] blocks2x2 = infer2x2.getBlocks();
		for (int k=0; k<infer2x2.getLatentDim(); k++) {
			
			NKPDist[] block2x2 = blocks2x2[k];
			
			BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			
			for (int i=0; i<4; i++) {
				NKPDist dist = block2x2[i];
				double myu = dist.getPredictiveMyu();
				int remove = 255 - (int)(255 * myu);
				Color color = new Color(remove, remove, 255);
				g.setColor(color);
				if (i==0) {
					g.fillRect(0, 0, 20, 20);
				} else if (i==1) {
					g.fillRect(20, 0, 20, 20);
				} else if (i==2) {
					g.fillRect(0, 20, 20, 20);
				} else if (i==3) {
					g.fillRect(20, 20, 20, 20);
				}
				Color border = new Color(63, 63, 63);
				g.setColor(border);
				g.drawRect(0, 0, 39, 39);
			}
			
			File imgFile = new File(StringUtils.concatPath(options.getOutputDir(), "2x2_" + (k+1) + ".png"));
		    ImageIO.write(img, "png", imgFile);
		}
		
//		monitor.write("Press any key to exit...");
//		System.in.read();
		
		monitor.write("DONE.");
	}
}
