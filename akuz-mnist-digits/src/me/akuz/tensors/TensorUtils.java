package me.akuz.tensors;

public final class TensorUtils {

	public static final void checkNotEmpty(final Integer[] input) {
		if (input == null) {
			throw new NullPointerException("input");
		}
		if (input.length == 0) {
			throw new IllegalArgumentException(
				"Must specify at least one value in the Integer array");
		}
	}

	public static final void checkNotEmpty(final int[] input) {
		if (input == null) {
			throw new NullPointerException("input");
		}
		if (input.length == 0) {
			throw new IllegalArgumentException(
				"Must specify at least one value in the int array");
		}
	}

	public static final void checkNdimsMatch(
			final int expected, 
			final int actual,
			final String message) {
		
		if (expected != actual) {
			throw new IllegalArgumentException(
				"Dimensionality mismatch (" + message + 
				"): expected ndim " + expected + ", got ndim " + actual);
		}
	}

}
