package me.akuz.core.math;

public final class StudentT {
	
	public static final double logLike(
			final double nyu,
			final double mean,
			final double sigma,
			final double value) {
		
		double logLike = 0.0;
		
		logLike += GammaFunction.lnGamma((nyu + 1.0) / 2.0);
		
		logLike -= GammaFunction.lnGamma(nyu / 2.0);
		
		logLike -= 0.5 * Math.log(nyu*Math.PI);
		
		logLike -= Math.log(sigma);
		
		logLike -= (nyu + 1.0) / 2.0 
				* Math.log(1.0 + Math.pow((value - mean)/sigma, 2) / nyu);
				
		return logLike;
	}

}
