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

	private final int IMAGE_SIZE = 28;
	
	private final int DIM2  = 8;
	private final int ITER2 = 5;
	
	private final int DIM4  = 16;
	private final int ITER4 = 10;
	
	private final int DIM8  = 32;
	private final int ITER8 = 10;
	
	private final int DIM16  = 64;
	private final int ITER16 = 20;
	
	private final double LOG_LIKE_CHANGE_THRESHOLD = 0.001;
	
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
		InferNKP infer2x2 = new InferNKP(monitor, digits, 1, DIM2, ITER2, LOG_LIKE_CHANGE_THRESHOLD);
		
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
		InferHDP infer4x4 = new InferHDP(monitor, infer2x2.getFeatureImages(), 2, DIM2, DIM4, ITER4, LOG_LIKE_CHANGE_THRESHOLD);
		
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

		
		monitor.write("Interring 8x8 blocks...");
		InferHDP infer8x8 = new InferHDP(monitor, infer4x4.getFeatureImages(), 4, DIM4, DIM8, ITER8, LOG_LIKE_CHANGE_THRESHOLD);
		
		monitor.write("Saving 8x8 features...");
		DirDist[][] blocks8x8 = infer8x8.getFeatureBlocks();
		for (int k8=0; k8<blocks8x8.length; k8++) {
			
			DirDist[] block8x8 = blocks8x8[k8];
			
			// calculate expected pixel values
			double[][] myus = new double[8][8];
			
			for (int l8=0; l8<4; l8++) {
				
				final int row8;
				final int col8;
				switch (l8) {
				case 0:
					row8 = 0;
					col8 = 0;
					break;
				case 1:
					row8 = 0;
					col8 = 4;
					break;
				case 2:
					row8 = 4;
					col8 = 0;
					break;
				case 3:
					row8 = 4;
					col8 = 4;
					break;
				default:
					throw new IllegalStateException();
				}
				
				double[] block4x4Probs = block8x8[l8].getPosterior();
				
				for (int k4=0; k4<blocks4x4.length; k4++) {

					for (int l4=0; l4<4; l4++) {
						
						final int row4;
						final int col4;
						switch (l4) {
						case 0:
							row4 = 0;
							col4 = 0;
							break;
						case 1:
							row4 = 0;
							col4 = 2;
							break;
						case 2:
							row4 = 2;
							col4 = 0;
							break;
						case 3:
							row4 = 2;
							col4 = 2;
							break;
						default:
							throw new IllegalStateException();
						}
						
						double[] block2x2Probs = blocks4x4[k4][l4].getPosterior();
						
						for (int k2=0; k2<block2x2Probs.length; k2++) {
							
							double pixelProb = block4x4Probs[k4] * block2x2Probs[k2];
							
							for (int l2=0; l2<4; l2++) {
								
								double myu = blocks2x2[k2][l2].getPosteriorMyu();
								
								final int row2 = l2 / 2;
								final int col2 = l2 % 2;
								
								myus[row8 + row4 + row2][col8 + col4 + col2] += pixelProb * myu;
							}
						}
					}
				}
			}
			
			
			BufferedImage img = new BufferedImage(160, 160, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			
			for (int row=0; row<8; row++) {
				for (int col=0; col<8; col++) {
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
				g.drawRect(0, 0, 159, 159);
			}
			
			File imgFile = new File(StringUtils.concatPath(options.getOutputDir(), "8x8_" + (k8+1) + ".png"));
		    ImageIO.write(img, "png", imgFile);
		}

		monitor.write("Interring 16x16 blocks...");
		InferHDP infer16x16 = new InferHDP(monitor, infer8x8.getFeatureImages(), 8, DIM8, DIM16, ITER16, LOG_LIKE_CHANGE_THRESHOLD);
		
		monitor.write("Saving 16x16 features...");
		DirDist[][] blocks16x16 = infer16x16.getFeatureBlocks();
		for (int k16=0; k16<blocks16x16.length; k16++) {
			
			DirDist[] block16x16 = blocks16x16[k16];
			
			// calculate expected pixel values
			double[][] myus = new double[16][16];
			
			for (int l16=0; l16<4; l16++) {
				
				final int row16;
				final int col16;
				switch (l16) {
				case 0:
					row16 = 0;
					col16 = 0;
					break;
				case 1:
					row16 = 0;
					col16 = 8;
					break;
				case 2:
					row16 = 8;
					col16 = 0;
					break;
				case 3:
					row16 = 8;
					col16 = 8;
					break;
				default:
					throw new IllegalStateException();
				}

				double[] block8x8Probs = block16x16[l16].getPosterior();
				
				for (int k8=0; k8<blocks8x8.length; k8++) {
					
					for (int l8=0; l8<4; l8++) {
						
						final int row8;
						final int col8;
						switch (l8) {
						case 0:
							row8 = 0;
							col8 = 0;
							break;
						case 1:
							row8 = 0;
							col8 = 4;
							break;
						case 2:
							row8 = 4;
							col8 = 0;
							break;
						case 3:
							row8 = 4;
							col8 = 4;
							break;
						default:
							throw new IllegalStateException();
						}
						
						double[] block4x4Probs = blocks8x8[k8][l8].getPosterior();
						
						for (int k4=0; k4<blocks4x4.length; k4++) {
		
							for (int l4=0; l4<4; l4++) {
								
								final int row4;
								final int col4;
								switch (l4) {
								case 0:
									row4 = 0;
									col4 = 0;
									break;
								case 1:
									row4 = 0;
									col4 = 2;
									break;
								case 2:
									row4 = 2;
									col4 = 0;
									break;
								case 3:
									row4 = 2;
									col4 = 2;
									break;
								default:
									throw new IllegalStateException();
								}
								
								double[] block2x2Probs = blocks4x4[k4][l4].getPosterior();
								
								for (int k2=0; k2<block2x2Probs.length; k2++) {
									
									double pixelProb = block8x8Probs[k8] * block4x4Probs[k4] * block2x2Probs[k2];
									
									for (int l2=0; l2<4; l2++) {
										
										double myu = blocks2x2[k2][l2].getPosteriorMyu();
										
										final int row2 = l2 / 2;
										final int col2 = l2 % 2;
										
										myus[row16 + row8 + row4 + row2][col16 + col8 + col4 + col2] += pixelProb * myu;
									}
								}
							}
						}
					}
					
				}
			}
			
			BufferedImage img = new BufferedImage(320, 320, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			
			for (int row=0; row<16; row++) {
				for (int col=0; col<16; col++) {
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
				g.drawRect(0, 0, 319, 319);
			}
			
			File imgFile = new File(StringUtils.concatPath(options.getOutputDir(), "XxX_" + (k16+1) + ".png"));
		    ImageIO.write(img, "png", imgFile);
		}

//		monitor.write("Press any key to exit...");
//		System.in.read();
		
		monitor.write("DONE.");
	}
}
