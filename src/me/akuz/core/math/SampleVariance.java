package me.akuz.core.math;

public final class SampleVariance {
	
	private final SampleAverage _avg;
	private final SampleAverage _avgSq;

	public SampleVariance() {
		_avg = new SampleAverage();
		_avgSq = new SampleAverage();
	}
	
	public void add(double value) {
		_avg.add(value);
		_avgSq.add(value*value);
	}
	
	public double getMean() {
		return _avg.getMean();
	}
	
	public double getVariance() {
		return _avgSq.getMean() - Math.pow(_avg.getMean(), 2);
	}
	
	public double getSigma() {
		return Math.sqrt(getVariance());
	}
	
	public double getCount() {
		return _avg.getCount();
	}

}
