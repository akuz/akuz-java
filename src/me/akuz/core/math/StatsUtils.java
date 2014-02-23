package me.akuz.core.math;

import java.security.InvalidParameterException;
import java.util.Random;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public final class StatsUtils {
	
	private static final double _naturalLogOfTwo = Math.log(2);

	/**
	 * Right distribution should be non-zero everywhere where left distribution is non-zero.
	 */
	public static final double calcKLDiv(double[] left, double[] right) {
		
		if (left.length != right.length) {
			throw new InvalidParameterException("Arrays should have the same number of elements");
		}
		double divKL = 0.0;
		for (int i=0; i<left.length; i++) {
			double leftProb = left[i];
			double rightProb = right[i];
			if (leftProb > 0) {
				if (rightProb == 0) {
					throw new InvalidParameterException(
							"KL-divergence error: right distribution is zero on a " +
							"set that has non-zero probability in left distribution");
				}
				divKL += leftProb * (Math.log(leftProb) - Math.log(rightProb));
			}
		}
		return divKL;
	}
	
	/**
	 * Safe for distributions with zero probabilities on any of the events.
	 */
	public static final double calcJSDist(double[] left, double[] right) {
		
		if (left.length != right.length) {
			throw new InvalidParameterException("Arrays should have the same number of elements");
		}
		double divKL1 = 0.0;
		double divKL2 = 0.0;
		for (int i=0; i<left.length; i++) {
			double leftProb = left[i];
			double rightProb = right[i];
			if (leftProb > 0) {
				divKL1 += leftProb * (Math.log(leftProb) - Math.log((rightProb + leftProb)/2.0));
			}
			if (rightProb > 0) {
				divKL2 += rightProb * (Math.log(rightProb) - Math.log((rightProb + leftProb)/2.0));
			}
		}
		return (divKL1 + divKL2)/2.0;
	}

	public static final void normalize(double[] probs) {
		
		double total = 0;
		for (int i=0; i<probs.length; i++) {
			total += Math.abs(probs[i]);
		}
		if (total > 0) {
			for (int i=0; i<probs.length; i++) {
				probs[i] = probs[i] / total;
			}
		} else {
			for (int i=0; i<probs.length; i++) {
				probs[i] = 1.0 / (double)probs.length;
			}
		}
	}

	public static final double[] logLikesToProbs(double[] loglikes) {

		double[] probs = new double[loglikes.length];
		logLikesToProbsReplace(probs);
		return probs;
	}

	public static final void logLikesToProbsReplace(double[] loglikes) {
		
		double maxloglike = Double.NEGATIVE_INFINITY;
		for (int i=0; i<loglikes.length; i++) {
			double loglike = loglikes[i];
			if (maxloglike < loglike) {
				maxloglike = loglike;
			}
		}
		
		if (maxloglike > Double.NEGATIVE_INFINITY) {
			for (int i=0; i<loglikes.length; i++) {
				loglikes[i] = Math.exp(loglikes[i] - maxloglike);
			}
		}
		
		normalize(loglikes);
	}
	
	/**
	 * A smart trick to calculate LogSumExp(array) =
	 * log ( sum_value_in_array ( exp ( value ) ) ).
	 */
	public static final double logSumExp(double[] values) {
		double max = 0;
		for (int i=0; i<values.length; i++) {
			double value = values[i];
			if (i==0 || max < value) {
				max = value;
			}
		}
		double sum = 0;
		for (int i=0; i<values.length; i++) {
			double value = values[i];
			sum += Math.exp(value - max);
		}
		return max + Math.log(sum);
	}

	public static final double calcDistanceWeightGaussian(double distance, double sigma) {
		return Math.exp(-distance*distance/2.0/Math.pow(sigma, 2));
	}
	
	public static final double calcDistanceWeightExponential(double distance, double halfLife) {
		if (halfLife <= 0) {
			throw new IllegalArgumentException("Argument halfLife must be positive");
		}
		double lyamda = Math.log(2) / halfLife;
		return Math.exp(- lyamda * Math.abs(distance));
	}

	public static double log2(double value) {
		return Math.log(value)/_naturalLogOfTwo;
	}

	public static double log10(double value) {
		return Math.log10(value);
	}

	public static void calcCDFReplace(double[] pdf) {
		double cdf = 0.0;
		for (int i=0; i<pdf.length; i++) {
			cdf += pdf[i];
			pdf[i] = cdf;
		}
	}

	public static int nextDiscrete(Random rnd, double[] cdf) {
		int result = -1;
		double u = rnd.nextDouble();
		for (int i=0; i<cdf.length; i++) {
			if (u < cdf[i]) {
				result = i;
				break;
			}
		}
		if (result < 0) {
			throw new IllegalStateException("Could not generate random multinomial");
		}
		return result;
	}
	
	public static final Matrix calcSampleCovarianceMatrix(final Matrix x, final int startRow, final int endRow) {
		
		Matrix s = new Matrix(x.getColumnDimension(), x.getColumnDimension());
		for (int i=startRow; i<endRow; i++) {
			for (int j=0; j<x.getColumnDimension(); j++) {
				for (int k=0; k<x.getColumnDimension(); k++) {
					s.set(j, k, 
							s.get(j, k) + 
							x.get(i, j) * x.get(i, k) / x.getRowDimension());
				}
			}
		}
		return s;
	}
	
	public static final Matrix generalizedInverse(Matrix m) {
		
		SingularValueDecomposition svd = new SingularValueDecomposition(m);
		
		// will modify in-place
		Matrix inverseS = svd.getS();
		
		for (int i=0; i<inverseS.getRowDimension(); i++) {
			double value = inverseS.get(i, i);
			if (value > 0) {
				inverseS.set(i, i, 1.0 / value);
			} else {
				break;
			}
		}
		
		return svd.getV().times(inverseS).times(svd.getU().transpose());
	}
	
	public static final double pseudoDeterminant(Matrix m) {

		return pseudoDeterminant(m, 1.0);
	}

	public static final double pseudoDeterminant(Matrix m, double matrixMultiplier) {
		
		SingularValueDecomposition svd = new SingularValueDecomposition(m);
		Matrix S = svd.getS();
		
		double det = 1;
		for (int i=0; i<S.getRowDimension(); i++) {

			double singularValue = S.get(i, i);
			if (singularValue > 0) {
				
				det *= matrixMultiplier * singularValue;
				
			} else {
				break;
			}
		}

		return det;
	}

}
