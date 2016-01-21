package me.akuz.mnist.digits.visor;

public final class VisorUtils {

	public static final double clip01(final double value) {
		if (value < 0.000001) {
			return 0.000001;
		} else if (value > 0.999999) {
			return 0.999999;
		} else {
			return value;
		}
	}

	public static final double clip55(final double value) {
		if (value < -0.444449) {
			return -0.444449;
		} else if (value > 0.444449) {
			return 0.444449;
		} else {
			return value;
		}
	}
}
