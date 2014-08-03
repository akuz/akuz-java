package me.akuz.core.math;

public final class SigmoidFunc {

	public final static double sigmoid(double t) {
		return 2.0 * (1.0 / (1.0 + Math.exp(-t)) - 0.5);
	}
}
