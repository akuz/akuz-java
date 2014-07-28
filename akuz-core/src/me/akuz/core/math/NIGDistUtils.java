package me.akuz.core.math;

public final class NIGDistUtils {
	
	public static final NIGDist[] createPriorsOnIntervalEvenly(
			final int count,
			final double min,
			final double max,
			final int priorSamples) {
		
		if (min >= max) {
			throw new IllegalArgumentException("Argument min must be < max");
		}
		
		final NIGDist[] arr = new NIGDist[count];
		
		final double step = (max - min) / (count + 1);
		
		for (int i=0; i<arr.length; i++) {
			
			final double priorMean = min + (i + 1) * step;
			
			final double priorVariance = Math.pow(step / 3.0, 2);
			
			arr[i] = new NIGDist(priorMean, priorSamples, priorVariance, priorSamples);
		}
		
		return arr;
	}

}
