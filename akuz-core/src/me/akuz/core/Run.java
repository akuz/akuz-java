package me.akuz.core;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class Run {

	public static void main(String[] args) {
		
		System.out.println(majority_expected_value(10, 0.5));
		System.out.println(majority_expected_value(10000, 0.77));
		System.out.println(approx_majority_expected_value(10000, 0.77, 100));
	}
	
	public static double majority_expected_value(int N, double p) {

		BinomialDistribution bd = new BinomialDistribution(N, p);
		double expected_value = 0.0;
		for (int k=0; k<=N; k++) {
			expected_value += Math.max(k, N - k) * bd.probability(k);
		}
		return expected_value;
	}
	
	public static double approx_majority_expected_value(int N, double p, int slots) {
		
		return majority_expected_value(slots, p) / slots * N;
	}

}
