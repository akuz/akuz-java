package me.akuz.mnist.digits.hrp;

import me.akuz.core.geom.ByteImage;

public final class Sample {
	
	private final double[][] _sum;
	private final int[][] _counts;
	
	public Sample(int nrow, int ncol) {
		
		_sum = new double[nrow][];
		for (int i=0; i<nrow; i++) {
			_sum[i] = new double[ncol];
		}
		_counts = new int[nrow][];
		for (int i=0; i<nrow; i++) {
			_counts[i] = new int[ncol];
		}
	}
	
	public void add(final int i, final int j, final double intensity) {
		
		_sum[i][j] += intensity;
		_counts[i][j] += 1;
	}
	
	public ByteImage get() {
		
		ByteImage image = new ByteImage(_sum.length, _sum[0].length);
		for (int i=0; i<_sum.length; i++) {
			for (int j=0; j<_sum[0].length; j++) {
				
				if (_counts[i][j] > 0) {
					image.setIntensity(i, j, _sum[i][j] / _counts[i][j]);
				}
			}
		}
		return image;
	}

}
