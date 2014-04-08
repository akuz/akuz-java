package me.akuz.core.math;

public final class GammaApprox {

	private final static long[] _nums   = {1, -1,   1,    5,   -21,   -399,     869,    39325,     -334477,    -28717403,      59697183,    8400372435L,   -34429291905L,  -7199255611995L,   14631594576045L,  4251206967062925L};
	private final static long[] _denoms = {1,  8, 128, 1024, 32768, 262144, 4194304, 33554432, 2147483648L, 17179869184L, 274877906944L, 2199023255552L, 70368744177664L, 562949953421312L, 9007199254740992L, 72057594037927936L};

	/**
	 * Approximation of Gamma(x + 0.5) / Gamma(x). 
	 * Source: http://mathworld.wolfram.com/GammaFunction.html
	 */
	public final static double approxGammaPlusHalfByGammaFraction(double arg) {
		
		double sum = 1.0;
		double pow = 1.0;
		
		for (int i=1; i<_nums.length; i++) {
			
			pow /= arg;
			double part = (double)_nums[i] / (double)_denoms[i];
			sum += part * pow;
			
			if (Math.abs(pow) <= Double.MIN_NORMAL) {
				break;
			}
			if (Math.abs(part) <= Double.MIN_NORMAL) {
				break;
			}
		}

		return Math.sqrt(arg) * sum;
	}
	
	
}
