package me.akuz.mnist.digits;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import Jama.Matrix;

import me.akuz.core.FileUtils;
import me.akuz.core.StringUtils;
import me.akuz.core.geom.ByteImage;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.math.MatrixUtils;

public final class ProgramLogic {

	private static final int IMAGE_SIZE = 28;
	private static final int MAX_IMAGE_COUNT = 50;
	
	private static final int LAYER_ITER_COUNT = 3;
	private static final double LOG_LIKE_CHANGE_THRESHOLD = 0.001;
	
	private static final int DIM2  = 8;
	private static final int ITER2 = 5;
	
	private static final int DIM4  = 12;
	private static final int ITER4 = 6;
	
	private static final int DIM8  = 16;
	private static final int ITER8 = 7;
	
	private static final int DIM16  = 20;
	private static final int ITER16 = 8;
	
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
		List<Integer> numbers = new ArrayList<>();
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
					int number = Integer.parseInt(parts[0]);
					numbers.add(number);
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
				
				if (images.size() >= MAX_IMAGE_COUNT) {
					break;
				}
			}
		}
		
		monitor.write("Cleaning output dir...");
		FileUtils.cleanDir(options.getOutputDir());
		
		// initialize inferences
		InferNIG infer2x2   = null;
		InferHDP infer4x4   = null;
		InferHDP infer8x8   = null;
		InferHDP infer16x16 = null;
		
		for (int iter=1; iter<=LAYER_ITER_COUNT; iter++) {
			
			monitor.write("Interring 2x2 blocks [" + iter + ">>] ...");
			if (infer2x2 == null) {
				infer2x2 = new InferNIG(monitor, images, DIM2, 1);
			}
			if (infer4x4 != null) {
				infer2x2.setParentFeatureImages(
					infer4x4.getFeatureShift(), 
					infer4x4.getFeatureBlocks(), 
					infer4x4.getFeatureImages());
			}
			infer2x2.execute(monitor, ITER2, LOG_LIKE_CHANGE_THRESHOLD);
			
			monitor.write("Saving 2x2 features [" + iter + ">>] ...");
			SaveBlocksNIG.save2x2(
					infer2x2.getFeatureProbs(),
					infer2x2.getFeatureBlocks(),
					options.getOutputDir());
			
			monitor.write("Interring 4x4 blocks [" + iter + ">>] ...");
			if (infer4x4 == null) {
				infer4x4 = new InferHDP(infer2x2.getFeatureImages(), DIM2, DIM4, 2);
			}
			if (infer8x8 != null) {
				infer4x4.setParentFeatureImages(
						infer8x8.getFeatureShift(), 
						infer8x8.getFeatureBlocks(), 
						infer8x8.getFeatureImages());
			}
			infer4x4.execute(monitor, ITER4, LOG_LIKE_CHANGE_THRESHOLD);
			
			monitor.write("Saving 4x4 features [" + iter + ">>] ...");
			SaveBlocksNIG.save4x4(
					infer4x4.getFeatureProbs(),
					infer4x4.getFeatureBlocks(), 
					infer2x2.getFeatureBlocks(), 
					options.getOutputDir());
		
			monitor.write("Interring 8x8 blocks [" + iter + ">>] ...");
			if (infer8x8 == null) {
				infer8x8 = new InferHDP(infer4x4.getFeatureImages(), DIM4, DIM8, 4);
			}
			if (infer16x16 != null) {
				infer8x8.setParentFeatureImages(
						infer16x16.getFeatureShift(), 
						infer16x16.getFeatureBlocks(), 
						infer16x16.getFeatureImages());
			}
			infer8x8.execute(monitor, ITER8, LOG_LIKE_CHANGE_THRESHOLD);
			
			monitor.write("Saving 8x8 features [" + iter + ">>] ...");
			SaveBlocksNIG.save8x8(
					infer8x8.getFeatureProbs(), 
					infer8x8.getFeatureBlocks(), 
					infer4x4.getFeatureBlocks(), 
					infer2x2.getFeatureBlocks(),
					options.getOutputDir());
		
			monitor.write("Interring 16x16 blocks [" + iter + ">>] ...");
			if (infer16x16 == null) {
				infer16x16 = new InferHDP(infer8x8.getFeatureImages(), DIM8, DIM16, 8);
			}
			infer16x16.execute(monitor, ITER16, LOG_LIKE_CHANGE_THRESHOLD);
			
			monitor.write("Saving 16x16 features [" + iter + ">>] ...");
			SaveBlocksNIG.save16x16(
					infer16x16.getFeatureProbs(), 
					infer16x16.getFeatureBlocks(), 
					infer8x8.getFeatureBlocks(), 
					infer4x4.getFeatureBlocks(), 
					infer2x2.getFeatureBlocks(), 
					options.getOutputDir());
			
			monitor.write("Interring 8x8 blocks [" + iter + "<<] ...");
			infer8x8.setParentFeatureImages(
					infer16x16.getFeatureShift(), 
					infer16x16.getFeatureBlocks(), 
					infer16x16.getFeatureImages());
			infer8x8.execute(monitor, ITER8, LOG_LIKE_CHANGE_THRESHOLD);
			
			monitor.write("Saving 8x8 features [" + iter + "<<] ...");
			SaveBlocksNIG.save8x8(
					infer8x8.getFeatureProbs(), 
					infer8x8.getFeatureBlocks(), 
					infer4x4.getFeatureBlocks(), 
					infer2x2.getFeatureBlocks(),
					options.getOutputDir());
			
			monitor.write("Interring 4x4 blocks [" + iter + "<<] ...");
			infer4x4.setParentFeatureImages(
					infer8x8.getFeatureShift(), 
					infer8x8.getFeatureBlocks(), 
					infer8x8.getFeatureImages());
			infer4x4.execute(monitor, ITER4, LOG_LIKE_CHANGE_THRESHOLD);
			
			monitor.write("Saving 4x4 features [" + iter + "<<] ...");
			SaveBlocksNIG.save4x4(
					infer4x4.getFeatureProbs(), 
					infer4x4.getFeatureBlocks(), 
					infer2x2.getFeatureBlocks(),
					options.getOutputDir());
		}

		monitor.write("Interring 2x2 blocks [last>>] ...");
		if (infer4x4 != null) {
			infer2x2.setParentFeatureImages(
				infer4x4.getFeatureShift(), 
				infer4x4.getFeatureBlocks(), 
				infer4x4.getFeatureImages());
		}
		infer2x2.execute(monitor, ITER2, LOG_LIKE_CHANGE_THRESHOLD);
		
		monitor.write("Saving 2x2 features [last>>] ...");
		SaveBlocksNIG.save2x2(
				infer2x2.getFeatureProbs(),
				infer2x2.getFeatureBlocks(),
				options.getOutputDir());
		
		final String txtOutputDir = StringUtils.concatPath(options.getOutputDir(), "txt");
		FileUtils.isDirExistsOrCreate(txtOutputDir);
		final DecimalFormat fileFmt = new DecimalFormat("0000");
		final DecimalFormat idxFmt = new DecimalFormat("00");
		final InferHDP infer = infer16x16;
		final Map<Integer, Double> numberCounts = new HashMap<>();
		final Map<Integer, AvgArr> numberDists = new HashMap<>();
		final List<double[]> imageFeatureProbsList = new ArrayList<>();
		{
			final List<FeatureImage> featureImages = infer.getFeatureImages();
			final Map<Integer, Integer> orderMap = SaveBlocksNIG.getOrderMap(infer.getFeatureProbs());
			for (int f=0; f<featureImages.size(); f++) {
				
				final Integer number = numbers.get(f);
				final FeatureImage featureImage = featureImages.get(f);
				final AvgArr imageFeatureProbs = new AvgArr(infer.getFeatureDim());
				
				final StringBuilder sb = new StringBuilder();
				for (int i=0; i<featureImage.getRowCount(); i++) {
					for (int j=0; j<featureImage.getColCount(); j++) {
						
						if (j > 0) {
							sb.append(" ");
						}
						double[] probs = featureImage.getFeatureProbs(i, j);
						imageFeatureProbs.add(probs);
						int maxIdx = -1;
						double maxProb = 0;
						for (int k=0; k<probs.length; k++) {
							double prob = probs[k];
							if (maxIdx < 0 || maxProb < prob) {
								maxIdx = k;
								maxProb = prob;
							}
						}
						sb.append(idxFmt.format(orderMap.get(maxIdx) + 1));
					}
					sb.append("\n");
				}
				imageFeatureProbsList.add(imageFeatureProbs.get());
				
				AvgArr numberDist = numberDists.get(number);
				if (numberDist == null) {
					numberDist = new AvgArr(infer.getFeatureDim());
					numberDists.put(number, numberDist);
				}
				numberDist.add(imageFeatureProbs.get());
				
				Double numberCount = numberCounts.get(number);
				if (numberCount == null) {
					numberCount = 0.0;
				}
				numberCounts.put(number, numberCount + 1.0);
				
				final String txtFileName = StringUtils.concatPath(txtOutputDir, fileFmt.format(f+1) + "_" + numbers.get(f) + ".txt");
				FileUtils.writeEntireFile(txtFileName, sb.toString());
			}
		}

		final String mtxOutputDir = StringUtils.concatPath(options.getOutputDir(), "mtx");
		FileUtils.isDirExistsOrCreate(mtxOutputDir);
		
		Matrix mNumberProb = new Matrix(10, 1);
		for (Integer number : numberCounts.keySet()) {
			mNumberProb.set(number, 0, numberCounts.get(number) / imageFeatureProbsList.size());
		}
		if (!MatrixUtils.isBoundedPositive(mNumberProb)) {
			System.out.println("Matrix mNumberProb is not bounded-positive");
		}
		MatrixUtils.writeMatrix(StringUtils.concatPath(mtxOutputDir, "mNumberProb.csv"), mNumberProb);
		
		Matrix mNumberFeatureProb = new Matrix(10, infer.getFeatureDim());
		for (Integer number : numberDists.keySet()) {
			
			double[] numberDist = numberDists.get(number).get();
			for (int j=0; j<numberDist.length; j++) {
				mNumberFeatureProb.set(number, j, numberDist[j]);
			}
		}
		if (!MatrixUtils.isBoundedPositive(mNumberFeatureProb)) {
			System.out.println("Matrix mNumberFeatureProb is not bounded-positive");
		}
		MatrixUtils.writeMatrix(StringUtils.concatPath(mtxOutputDir, "mNumberFeatureProb.csv"), mNumberFeatureProb);
		
		// calculate precision on training data
		int correctCount = 0;
		for (int i=0; i<imageFeatureProbsList.size(); i++) {

			double[] dist = imageFeatureProbsList.get(i);

			double[] klDivs = new double[10];
			for (int n=0; n<10; n++) {
				double klDiv = 0;
				for (int k=0; k<dist.length; k++) {
					
					double prob1 = mNumberFeatureProb.get(n, k);
					double prob2 = dist[k];
					klDiv += prob1 * (Math.log(prob1) - Math.log(prob2)) 
						   + prob2 * (Math.log(prob2) - Math.log(prob1));
				}
				klDivs[n] = klDiv;
			}
			
			int guessN = -1;
			double minKLDiv = 0;
			for (int n=0; n<10; n++) {
				if (guessN < 0 || minKLDiv > klDivs[n]) {
					guessN = n;
					minKLDiv = klDivs[n];
				}
			}
			
			if (guessN == numbers.get(i).intValue()) {
				correctCount++;
			}
		}
		final double precision = (double)correctCount / (double)imageFeatureProbsList.size();
		System.out.println("Training data precision: " + precision);

		//		monitor.write("Press any key to exit...");
//		System.in.read();
		
		monitor.write("DONE.");
	}
}
