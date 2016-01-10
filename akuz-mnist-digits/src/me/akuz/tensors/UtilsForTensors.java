package me.akuz.tensors;

public final class UtilsForTensors {

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

	public static final void checkNdimsMatch(final int ndim1, final int ndim2) {
		if (ndim1 != ndim2) {
			throw new IllegalArgumentException(
				"Dimensionality mismatch: " + ndim1 + " vs " + ndim2);
		}
	}

}
