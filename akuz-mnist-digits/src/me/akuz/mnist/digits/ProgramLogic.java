package me.akuz.mnist.digits;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Jama.Matrix;
import me.akuz.core.FileUtils;
import me.akuz.core.StringUtils;
import me.akuz.core.geom.BWImage;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.math.AvgArr;
import me.akuz.core.math.MatrixUtils;
import me.akuz.core.math.NIGDist;
import me.akuz.mnist.digits.load.MNIST;

public final class ProgramLogic {

	private static final int MAX_IMAGE_COUNT = 500;
	
	private static final int LAYER_ITER_COUNT = 3;
	private static final double LOG_LIKE_CHANGE_THRESHOLD = 0.001;
	
	private static final int DIM1  = 8;
	private static final int ITER1 = 5;
	
	private static final int DIM2  = 8;
	private static final int ITER2 = 5;
	
	private static final int DIM4  = 16;
	private static final int ITER4 = 5;
	
	private static final int DIM8  = 32;
	private static final int ITER8 = 5;
	
	private static final int DIM16  = 64;
	private static final int ITER16 = 5;
	
	public ProgramLogic() {
	}

	public void execute(Monitor parentMonitor, ProgramOptions options) throws Exception {
		
		final LocalMonitor monitor = new LocalMonitor(this.getClass().getSimpleName(), parentMonitor);
		
		monitor.write("Checking output dir...");
		if (!FileUtils.isDirExistsOrCreate(options.getOutputDir())) {
			throw new IOException("Could not create output dir: " + options.getOutputDir());
		}
		
		monitor.write("Loading training data...");
		List<Integer> digits = new ArrayList<>();
		List<BWImage> images = new ArrayList<>();
		MNIST.load_train(options.getTrainFile(), digits, images, MAX_IMAGE_COUNT);

		monitor.write("Cleaning output dir...");
		FileUtils.cleanDir(options.getOutputDir());
		
		// initialize inferences
		InferCAT infer1x1   = null;
		InferNIG infer2x2   = null;
		InferHDP infer4x4   = null;
		InferHDP infer8x8   = null;
		InferHDP infer16x16 = null;
		
		for (int iter=1; iter<=LAYER_ITER_COUNT; iter++) {
			
			monitor.write("Interring 1x1 blocks [" + iter + ">>] ...");
			if (infer1x1 == null) {
				infer1x1 = new InferCAT(images, DIM1);
			}
			infer1x1.execute(monitor, ITER1, LOG_LIKE_CHANGE_THRESHOLD);
			
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
		final Map<Integer, NIGDist[]> numberFeatureDists = new HashMap<>();
		final List<double[]> imageFeatureProbsList = new ArrayList<>();
		{
			final List<ProbImage> featureImages = infer.getFeatureImages();
			final Map<Integer, Integer> orderMap = SaveBlocksNIG.getOrderMap(infer.getFeatureProbs());
			for (int f=0; f<featureImages.size(); f++) {
				
				final Integer number = digits.get(f);
				final ProbImage featureImage = featureImages.get(f);
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
				
				NIGDist[] numberFeatureDist = numberFeatureDists.get(number);
				if (numberFeatureDist == null) {
					numberFeatureDist = new NIGDist[infer.getFeatureDim()];
					numberFeatureDists.put(number, numberFeatureDist);
					for (int k=0; k<numberFeatureDist.length; k++) {
						numberFeatureDist[k] = new NIGDist(1.0 / infer.getFeatureDim(), 0.1, 0.25, 0.1);
					}
				}
				for (int k=0; k<numberFeatureDist.length; k++) {
					numberFeatureDist[k].addObservation(imageFeatureProbs.get()[k]);
				}
				
				Double numberCount = numberCounts.get(number);
				if (numberCount == null) {
					numberCount = 0.0;
				}
				numberCounts.put(number, numberCount + 1.0);
				
				final String txtFileName = StringUtils.concatPath(txtOutputDir, fileFmt.format(f+1) + "_" + digits.get(f) + ".txt");
				FileUtils.writeEntireFile(txtFileName, sb.toString());
			}
		}

		final String mtxOutputDir = StringUtils.concatPath(options.getOutputDir(), "mtx");
		FileUtils.isDirExistsOrCreate(mtxOutputDir);
		
		Matrix mNumberProb = new Matrix(10, 1);
		for (Integer number : numberCounts.keySet()) {
			mNumberProb.set(number, 0, (double)numberCounts.get(number) / (double)imageFeatureProbsList.size());
		}
		if (!MatrixUtils.isBoundedPositive(mNumberProb)) {
			System.out.println("Matrix mNumberProb is not bounded-positive");
		}
		MatrixUtils.writeMatrix(StringUtils.concatPath(mtxOutputDir, "mNumberProb.csv"), mNumberProb);
		
		// calculate precision on training data
		int correctCount = 0;
		for (int i=0; i<imageFeatureProbsList.size(); i++) {

			double[] featureProbs = imageFeatureProbsList.get(i);
			
			double[] logLike = new double[10];
			for (int number=0; number<logLike.length; number++) {
				
				logLike[number] = Math.log(mNumberProb.get(number, 0));
				
				NIGDist[] numberFeatureDist = numberFeatureDists.get(number);
				for (int k=0; k<numberFeatureDist.length; k++) {

					logLike[number] += Math.log(numberFeatureDist[k].getProb(featureProbs[k]));
				}
			}
			
			int guessN = -1;
			double maxLogLike = 0;
			for (int number=0; number<10; number++) {
				if (guessN < 0 || maxLogLike < logLike[number]) {
					guessN = number;
					maxLogLike = logLike[number];
				}
			}
			
			int correctN = digits.get(i).intValue();
			if (guessN == correctN) {
				correctCount++;
			}
			
			monitor.write("  " + correctN + " -> " + guessN);
		}
		final double precision = (double)correctCount / (double)imageFeatureProbsList.size();
		System.out.println("Training data precision: " + precision);

		//		monitor.write("Press any key to exit...");
//		System.in.read();
		
		monitor.write("DONE.");
	}
}
