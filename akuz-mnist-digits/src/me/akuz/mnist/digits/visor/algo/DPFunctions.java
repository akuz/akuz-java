package me.akuz.mnist.digits.visor.algo;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.GammaFunction;
import me.akuz.core.math.StatsUtils;

public final class DPFunctions {
	
	public static final DPMetaInfo initNoisyFlatDP(
			final double alpha,
			final double[] probs,
			final int startIdx,
			final int length) {
		
		Random rnd = ThreadLocalRandom.current();
		for (int idx=0; idx<length; idx++) {
			probs[idx] = 1.0 + rnd.nextDouble()*0.01;
		}
		StatsUtils.normalizeInPlace(probs, startIdx, length);
		
		double logNorm = 0.0;
		double alphaProbSum = 0.0;
		for (int idx=0; idx<length; idx++) {
			final double alphaProb = alpha*probs[startIdx + idx];
			logNorm -= GammaFunction.lnGamma(alphaProb);
			alphaProbSum += alphaProb;
		}
		logNorm += GammaFunction.lnGamma(alphaProbSum);
		
		return new DPMetaInfo(alpha, logNorm);
	}	

}
