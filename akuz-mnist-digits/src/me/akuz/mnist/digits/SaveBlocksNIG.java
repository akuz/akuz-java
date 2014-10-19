package me.akuz.mnist.digits;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.StringUtils;
import me.akuz.core.math.DirDist;
import me.akuz.core.math.NIGDist;
import me.akuz.core.math.StatsUtils;

public final class SaveBlocksNIG {

	private static final DecimalFormat fmt = new DecimalFormat("00");

	public static final Map<Integer, Integer> getOrderMap(double[] probs) {
		List<Pair<Integer, Double>> list = new ArrayList<>();
		for (int i=0; i<probs.length; i++) {
			list.add(new Pair<Integer, Double>(i, probs[i]));
		}
		if (list.size() > 1) {
			Collections.sort(list, new PairComparator<Integer, Double>(SortOrder.Desc));
		}
		Map<Integer, Integer> map = new HashMap<>();
		for (int i=0; i<list.size(); i++) {
			map.put(list.get(i).v1(), i);
		}
		return map;
	}
	
	public static final void save2x2(
			double[] featureProbs2x2,
			NIGDist[][] featureBlocks2x2, 
			String outputDir) throws IOException {
		
		Map<Integer, Integer> orderMap = getOrderMap(featureProbs2x2);

		for (int k2=0; k2<featureBlocks2x2.length; k2++) {
			
			NIGDist[] block2x2 = featureBlocks2x2[k2];
			
			BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			
			final Color backColor = new Color(255, 255, 0);
			g.setColor(backColor);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			
			for (int l=0; l<4; l++) {
				
				final NIGDist dist = block2x2[l];
				final double myu = dist.getMeanMode();
				final double sigma = Math.sqrt(dist.getVarianceMode());
				
				final int remove = 255 - (int)(255 * myu);
				final int alpha = (int)(255.0 * StatsUtils.calcDistanceWeightGaussian(sigma, 0.3));
				final Color color = new Color(remove, remove, 255, alpha);
				g.setColor(color);
				
				final int row = l / 2;
				final int col = l % 2;
				final int x = col * 20;
				final int y = row * 20;
				g.fillRect(x, y, 20, 20);
				
				final Color border = new Color(63, 63, 63);
				g.setColor(border);
				g.drawRect(0, 0, 39, 39);
			}
			
			File imgFile = new File(StringUtils.concatPath(outputDir, "02x02_" + fmt.format(orderMap.get(k2)+1) + ".png"));
		    ImageIO.write(img, "png", imgFile);
		}
	}
	
	public static final void save4x4(
			double[] featureProbs4x4, 
			DirDist[][] featureBlocks4x4, 
			NIGDist[][] featureBlocks2x2, 
			String outputDir) throws IOException {

		Map<Integer, Integer> orderMap = getOrderMap(featureProbs4x4);

		for (int k4=0; k4<featureBlocks4x4.length; k4++) {
			
			DirDist[] block4x4 = featureBlocks4x4[k4];
			
			// calculate expected pixel values
			double[][] myus = new double[4][4];
			double[][] vars = new double[4][4];
			
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
				double[] block2x2Probs = dist.getPosteriorMean();
				
				for (int k2=0; k2<block2x2Probs.length; k2++) {
					
					double pixelProb = block2x2Probs[k2];
					
					for (int l2=0; l2<4; l2++) {
						
						final double myu = featureBlocks2x2[k2][l2].getMeanMode();
						final double var = featureBlocks2x2[k2][l2].getVarianceMode();
						
						final int addRow = l2 / 2;
						final int addCol = l2 % 2;
						
						myus[startRow + addRow][startCol + addCol] += pixelProb * myu;
						vars[startRow + addRow][startCol + addCol] += pixelProb * var;
					}
				}
			}
			
			BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			
			final Color backColor = new Color(255, 255, 0);
			g.setColor(backColor);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			
			for (int row=0; row<4; row++) {
				for (int col=0; col<4; col++) {
					final double myu = myus[row][col];
					final double sigma = Math.sqrt(vars[row][col]);
					int remove = 255 - (int)(255 * myu);
					final int alpha = (int)(255.0 * StatsUtils.calcDistanceWeightGaussian(sigma, 0.3));
					final Color color = new Color(remove, remove, 255, alpha);
					g.setColor(color);
					final int x = col * 20;
					final int y = row * 20;
					g.fillRect(x, y, 20, 20);
				}
				Color border = new Color(63, 63, 63);
				g.setColor(border);
				g.drawRect(0, 0, 79, 79);
			}
			
			File imgFile = new File(StringUtils.concatPath(outputDir, "04x04_" + fmt.format(orderMap.get(k4)+1) + ".png"));
		    ImageIO.write(img, "png", imgFile);
		}
	}
	
	public static final void save8x8(
			double[] featureProbs8x8,
			DirDist[][] featureBlocks8x8,
			DirDist[][] featureBlocks4x4,
			NIGDist[][] featureBlocks2x2,
			String outputDir) throws IOException {

		Map<Integer, Integer> orderMap = getOrderMap(featureProbs8x8);

		for (int k8=0; k8<featureBlocks8x8.length; k8++) {
			
			DirDist[] block8x8 = featureBlocks8x8[k8];
			
			// calculate expected pixel values
			double[][] myus = new double[8][8];
			double[][] vars = new double[8][8];
			
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
				
				double[] block4x4Probs = block8x8[l8].getPosteriorMean();
				
				for (int k4=0; k4<featureBlocks4x4.length; k4++) {

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
						
						double[] block2x2Probs = featureBlocks4x4[k4][l4].getPosteriorMean();
						
						for (int k2=0; k2<block2x2Probs.length; k2++) {
							
							double pixelProb = block4x4Probs[k4] * block2x2Probs[k2];
							
							for (int l2=0; l2<4; l2++) {
								
								double myu = featureBlocks2x2[k2][l2].getMeanMode();
								double var = featureBlocks2x2[k2][l2].getVarianceMode();
								
								final int row2 = l2 / 2;
								final int col2 = l2 % 2;
								
								myus[row8 + row4 + row2][col8 + col4 + col2] += pixelProb * myu;
								vars[row8 + row4 + row2][col8 + col4 + col2] += pixelProb * var;
							}
						}
					}
				}
			}
			
			
			BufferedImage img = new BufferedImage(160, 160, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			
			final Color backColor = new Color(255, 255, 0);
			g.setColor(backColor);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());

			for (int row=0; row<8; row++) {
				for (int col=0; col<8; col++) {
					double myu = myus[row][col];
					double sigma = Math.sqrt(vars[row][col]);
					int remove = 255 - (int)(255 * myu);
					final int alpha = (int)(255.0 * StatsUtils.calcDistanceWeightGaussian(sigma, 0.3));
					final Color color = new Color(remove, remove, 255, alpha);
					g.setColor(color);
					final int x = col * 20;
					final int y = row * 20;
					g.fillRect(x, y, 20, 20);
				}
				Color border = new Color(63, 63, 63);
				g.setColor(border);
				g.drawRect(0, 0, 159, 159);
			}
			
			File imgFile = new File(StringUtils.concatPath(outputDir, "08x08_" + fmt.format(orderMap.get(k8)+1) + ".png"));
		    ImageIO.write(img, "png", imgFile);
		}		
	}
	
	public static final void save16x16(
			double[] featureProbs16x16,
			DirDist[][] featureBlocks16x16,
			DirDist[][] featureBlocks8x8,
			DirDist[][] featureBlocks4x4,
			NIGDist[][] featureBlocks2x2,
			String outputDir) throws IOException {

		Map<Integer, Integer> orderMap = getOrderMap(featureProbs16x16);

		for (int k16=0; k16<featureBlocks16x16.length; k16++) {
			
			DirDist[] block16x16 = featureBlocks16x16[k16];
			
			// calculate expected pixel values
			double[][] myus = new double[16][16];
			double[][] vars = new double[16][16];
			
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

				double[] block8x8Probs = block16x16[l16].getPosteriorMean();
				
				for (int k8=0; k8<featureBlocks8x8.length; k8++) {
					
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
						
						double[] block4x4Probs = featureBlocks8x8[k8][l8].getPosteriorMean();
						
						for (int k4=0; k4<featureBlocks4x4.length; k4++) {
		
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
								
								double[] block2x2Probs = featureBlocks4x4[k4][l4].getPosteriorMean();
								
								for (int k2=0; k2<block2x2Probs.length; k2++) {
									
									double pixelProb = block8x8Probs[k8] * block4x4Probs[k4] * block2x2Probs[k2];
									
									for (int l2=0; l2<4; l2++) {
										
										double myu = featureBlocks2x2[k2][l2].getMeanMode();
										double var = featureBlocks2x2[k2][l2].getVarianceMode();
										
										final int row2 = l2 / 2;
										final int col2 = l2 % 2;
										
										myus[row16 + row8 + row4 + row2][col16 + col8 + col4 + col2] += pixelProb * myu;
										vars[row16 + row8 + row4 + row2][col16 + col8 + col4 + col2] += pixelProb * var;
									}
								}
							}
						}
					}
					
				}
			}
			
			BufferedImage img = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			
			final Color backColor = new Color(255, 255, 0);
			g.setColor(backColor);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			
			for (int row=0; row<16; row++) {
				for (int col=0; col<16; col++) {
					double myu = myus[row][col];
					double sigma = Math.sqrt(vars[row][col]);
					int remove = 255 - (int)(255 * myu);
					final int alpha = (int)(255.0 * StatsUtils.calcDistanceWeightGaussian(sigma, 0.3));
					final Color color = new Color(remove, remove, 255, alpha);
					g.setColor(color);
					final int x = col * 20;
					final int y = row * 20;
					g.fillRect(x, y, 20, 20);
				}
				Color border = new Color(63, 63, 63);
				g.setColor(border);
				g.drawRect(0, 0, 319, 319);
			}
			
			File imgFile = new File(StringUtils.concatPath(outputDir, "16x16_" + fmt.format(orderMap.get(k16)+1) + ".png"));
		    ImageIO.write(img, "png", imgFile);
		}
	}
	
}
