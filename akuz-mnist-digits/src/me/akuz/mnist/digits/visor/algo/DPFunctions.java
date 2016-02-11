package me.akuz.mnist.digits.visor.algo;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.GammaFunction;
import me.akuz.core.math.StatsUtils;

public final class DPFunctions {
	
	public static final double initNoisyFlatDP(
			final double alpha,
			final double[] fillAlphaProbs,
			final int fillStartIdx,
			final int fillLength) {
		
		Random rnd = ThreadLocalRandom.current();
		for (int idx=0; idx<fillLength; idx++) {
			fillAlphaProbs[idx] = 1.0 + rnd.nextDouble()*0.01;
		}
		StatsUtils.normalizeInPlace(fillAlphaProbs, fillStartIdx, fillLength);

		double logNorm = 0.0;
		double alphaProbSum = 0.0;
		for (int idx=0; idx<fillLength; idx++) {
			final int fillIdx = fillStartIdx + idx;
			final double alphaProb = alpha*fillAlphaProbs[fillIdx];
			fillAlphaProbs[fillIdx] = alphaProb;
			logNorm -= GammaFunction.lnGamma(alphaProb);
			alphaProbSum += alphaProb;
		}
		logNorm += GammaFunction.lnGamma(alphaProbSum);
		
		return logNorm;
	}	

}
