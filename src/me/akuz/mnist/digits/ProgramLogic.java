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
import me.akuz.core.math.DirDist;
import me.akuz.core.math.NKPDist;

public final class ProgramLogic {
	
	private final int DIM2 = 16;
	private final int DIM4 = 32;
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
		List<ByteImage> digits = new ArrayList<>();
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
					digits.add(digit);
				}
				counter += 1;
				
				if (counter >= 5000) {
					break;
				}
			}
		}
		
		monitor.write("Interring 2x2 blocks...");
		Infer2x2 infer2x2 = new Infer2x2(monitor, digits, DIM2, 5, 0.001);
		
		monitor.write("Cleaning output dir...");
		FileUtils.cleanDir(options.getOutputDir());
		
		monitor.write("Saving 2x2 features...");
		NKPDist[][] blocks2x2 = infer2x2.getFeatureBlocks();
		for (int k=0; k<blocks2x2.length; k++) {
			
			NKPDist[] block2x2 = blocks2x2[k];
			
			BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			
			for (int l=0; l<4; l++) {
				NKPDist dist = block2x2[l];
				double myu = dist.getPredictiveMyu();
				int remove = 255 - (int)(255 * myu);
				Color color = new Color(remove, remove, 255);
				g.setColor(color);
				final int row = l / 2;
				final int col = l % 2;
				final int x = col * 20;
				final int y = row * 20;
				g.fillRect(x, y, 20, 20);
				Color border = new Color(63, 63, 63);
				g.setColor(border);
				g.drawRect(0, 0, 39, 39);
			}
			
			File imgFile = new File(StringUtils.concatPath(options.getOutputDir(), "2x2_" + (k+1) + ".png"));
		    ImageIO.write(img, "png", imgFile);
		}
		
		monitor.write("Interring 4x4 blocks...");
		Infer4x4 infer4x4 = new Infer4x4(monitor, infer2x2.getFeatureImages(), DIM2, DIM4, 5, 0.001);
		
		monitor.write("Saving 4x4 features...");
		DirDist[][] blocks4x4 = infer4x4.getFeatureBlocks();
		for (int k=0; k<blocks4x4.length; k++) {
			
			DirDist[] block4x4 = blocks4x4[k];
			
			// calculate expected pixel values
			double[][] myus = new double[4][4];
			
			for (int l4=0; l4<4; l4++) {
				
				final int startRow;
				final int startCol;
				switch (l4) {
				case 0:
					startRow = 0;
					startCol = 0;
					break;
				case 1:
					startRow = 0;
					startCol = 2;
					break;
				case 2:
					startRow = 2;
					startCol = 0;
					break;
				case 3:
					startRow = 2;
					startCol = 2;
					break;
				default:
					throw new IllegalStateException();
				}
				
				DirDist dist = block4x4[l4];
				double[] block2x2Probs = dist.getPosterior();
				
				for (int k2=0; k2<block2x2Probs.length; k2++) {
					
					double block2x2Prob = block2x2Probs[k2];
					
					for (int l2=0; l2<4; l2++) {
						
						double myu = blocks2x2[k2][l2].getPosteriorMyu();
						
						final int addRow = l2 / 2;
						final int addCol = l2 % 2;
						
						myus[startRow + addRow][startCol + addCol] += block2x2Prob * myu;
					}
				}
			}
			
			
			BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			
			for (int row=0; row<4; row++) {
				for (int col=0; col<4; col++) {
					double myu = myus[row][col];
					int remove = 255 - (int)(255 * myu);
					Color color = new Color(remove, remove, 255);
					g.setColor(color);
					final int x = col * 20;
					final int y = row * 20;
					g.fillRect(x, y, 20, 20);
				}
				Color border = new Color(63, 63, 63);
				g.setColor(border);
				g.drawRect(0, 0, 79, 79);
			}
			
			File imgFile = new File(StringUtils.concatPath(options.getOutputDir(), "4x4_" + (k+1) + ".png"));
		    ImageIO.write(img, "png", imgFile);
		}

		monitor.write("Press any key to exit...");
		System.in.read();
		
		monitor.write("DONE.");
	}
}
