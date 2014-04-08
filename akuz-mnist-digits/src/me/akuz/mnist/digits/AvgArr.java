package me.akuz.mnist.digits;

public final class AvgArr {
	
	private final double[] _avg;
	private int _count;
	
	public AvgArr(int size) {
		_avg = new double[size];
	}
	
	public void add(double[] val) {
		if (_avg.length != val.length) {
			throw new IllegalArgumentException("Dimensionality mismatch");
		}
		for (int i=0; i<_avg.length; i++) {
			_avg[i] = _avg[i] / (double) (_count+1) * (double) _count 
					+  val[i] / (double) (_count+1);
		}
		_count++;
	}
	
	public double[] get() {
		return _avg;
	}

}
