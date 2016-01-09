package me.akuz.mnist.digits.tensor;

public final class TensorUtils {
	
	public static final int[] unboxIntegerArray(final Integer[] input) {
		if (input == null) {
			throw new NullPointerException("input");
		}
		final int[] arr = new int[input.length];
		for (int i=0; i<input.length; i++) {
			arr[i] = input[i];
		}
		return arr;
	}

	public static final void checkNotEmpty(final int[] input) {
		if (input == null) {
			throw new NullPointerException("input");
		}
		if (input.length == 0) {
			throw new IllegalArgumentException(
				"Must specify at least one value");
		}
	}

}
