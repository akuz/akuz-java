package me.akuz.mnist.digits;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.NKPDist;
import me.akuz.core.math.StatsUtils;

public final class Infer2x2 {
	
	private static final double PRIOR_MEAN = 0.5;
	private static final double PRIOR_MEAN_PRECISION = Math.pow(0.5, -2);
	private static final double PRECISION = Math.pow(0.1, -2);
	private static final double INIT_WEIGHT = 0.1;

	private static final int MAX_ITER = 10;
	private static final double LOG_LIKE_CHANGE_THRESHOLD = 0.001;
	
	private final int _latentDim;
	private final double[] _probs;
	private final NKPDist[][] _blocks;
	
	public Infer2x2(List<Digit> digits, int latentDim) {

		DecimalFormat fmt = new DecimalFormat("' '0.00000000;'-'0.00000000");
		
		if (latentDim < 2) {
			throw new IllegalArgumentException("Latent variables dimension must be >= 2");
		}
		_latentDim = latentDim;
		final Random rnd = ThreadLocalRandom.current();
		
		double[] currProbs = new double[_latentDim];
		Arrays.fill(currProbs, 1.0 / _latentDim);
		
		double[] nextProbs = new double[_latentDim];
		Arrays.fill(nextProbs, 0);
		
		NKPDist[][] currBlocks = new NKPDist[_latentDim][4];
		for (int k=0; k<_latentDim; k++) {
			for (int l=0; l<4; l++) {
				currBlocks[k][l] = new NKPDist(PRIOR_MEAN, PRIOR_MEAN_PRECISION, PRECISION);
				currBlocks[k][l].addObservation(rnd.nextDouble(), INIT_WEIGHT);
			}
		}
		
		NKPDist[][] nextBlocks = new NKPDist[_latentDim][4];
		for (int k=0; k<_latentDim; k++) {
			for (int l=0; l<4; l++) {
				nextBlocks[k][l] = new NKPDist(PRIOR_MEAN, PRIOR_MEAN_PRECISION, PRECISION);
			}
		}
		
		int iter = 0;
		double prevLogLike = Double.NaN;
		double[] logLikes = new double[_latentDim];
		
		System.out.println("Total " + digits.size() + " digits");

		while (true) {

			iter += 1;
			System.out.println("Iteration " + iter);

			for (int k=0; k<_latentDim; k++) {
				System.out.println("Latent state #" + (k+1));
				System.out.println("  Prob: " + fmt.format(currProbs[k]));
				for (int l=0; l<4; l++) {
					System.out.println("  Block #" + (l+1) + ": " + currBlocks[k][l]);
				}
			}
			
			
			double currLogLike = 0;
			
			for (int digitIndex=0; digitIndex<digits.size(); digitIndex++) {
				
				if (digitIndex % 100 == 0) {
					System.out.println(digitIndex + " digit index");
				}
				
				final Digit digit = digits.get(digitIndex);
				
				byte[][] data = digit.getData();
				for (int i=0; i<digit.getRowCount()-1; i+=2) {
					for (int j=0; j<digit.getColCount()-1; j+=2) {

						// init log likes
						for (int k=0; k<_latentDim; k++) {
							logLikes[k] = Math.log(currProbs[k]);
						}
						
						// add data log likes
						for (int k=0; k<_latentDim; k++) {
							{
								final int intValue = (int)(data[i][j] & 0xFF);
								final double value = intValue / 255.0;
								logLikes[k] += currBlocks[k][0].getLogProb(value);
							}
							{
								final int intValue = (int)(data[i][j+1] & 0xFF);
								final double value = intValue / 255.0;
								logLikes[k] += currBlocks[k][1].getLogProb(value);
							}
							{
								final int intValue = (int)(data[i+1][j] & 0xFF);
								final double value = intValue / 255.0;
								logLikes[k] += currBlocks[k][2].getLogProb(value);
							}
							{
								final int intValue = (int)(data[i+1][j+1] & 0xFF);
								final double value = intValue / 255.0;
								logLikes[k] += currBlocks[k][3].getLogProb(value);
							}
						}
						
						// add to current log likelihood
						currLogLike += StatsUtils.logSumExp(logLikes);

						// normalize probabilities of classes
						StatsUtils.logLikesToProbsReplace(logLikes);
						
						// add to next probs
						for (int k=0; k<_latentDim; k++) {
							nextProbs[k] += logLikes[k];
						}
						for (int k=0; k<_latentDim; k++) {
							if (logLikes[k] > 0) {
								{
									final int intValue = (int)(data[i][j] & 0xFF);
									final double value = intValue / 255.0;
									nextBlocks[k][0].addObservation(value, logLikes[k]);
								}
								{
									final int intValue = (int)(data[i][j+1] & 0xFF);
									final double value = intValue / 255.0;
									nextBlocks[k][1].addObservation(value, logLikes[k]);
								}
								{
									final int intValue = (int)(data[i+1][j] & 0xFF);
									final double value = intValue / 255.0;
									nextBlocks[k][2].addObservation(value, logLikes[k]);
								}
								{
									final int intValue = (int)(data[i+1][j+1] & 0xFF);
									final double value = intValue / 255.0;
									nextBlocks[k][3].addObservation(value, logLikes[k]);
								}
							}
						}
					}
				}
			}
			
			// normalize next probs
			StatsUtils.normalize(nextProbs);
			
			System.out.println("LogLike: " + currLogLike);
			
			// check if converged
			if (Double.isNaN(prevLogLike) == false &&
				(currLogLike < prevLogLike ||
				 Math.abs(prevLogLike - currLogLike) < LOG_LIKE_CHANGE_THRESHOLD)) {
				
				System.out.println("Converged.");
				break;
			}
			
			// check if max iterations
			if (iter > MAX_ITER) {

				System.out.println("Max iterations done.");
				break;
			}
			
			{
				double[] tmp = currProbs;
				currProbs = nextProbs;
				nextProbs = tmp;
				Arrays.fill(nextProbs, 0);
			}
			{
				NKPDist[][] tmp = currBlocks;
				currBlocks = nextBlocks;
				nextBlocks = tmp;
				for (int k=0; k<_latentDim; k++) {
					for (int l=0; l<4; l++) {
						nextBlocks[k][l] = new NKPDist(PRIOR_MEAN, PRIOR_MEAN_PRECISION, PRECISION);
					}
				}
			}
			
			prevLogLike = currLogLike;
		}
		
		_probs = currProbs;
		_blocks = currBlocks;
	}
	
	public int getLatentDim() {
		return _latentDim;
	}
	
	public double[] getProbs() {
		return _probs;
	}
	
	public NKPDist[][] getBlocks() {
		return _blocks;
	}
	

}
