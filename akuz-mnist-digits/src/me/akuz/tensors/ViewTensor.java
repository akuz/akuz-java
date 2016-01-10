package me.akuz.tensors;

/**
 * View tensor looking at some other tensor.
 *
 */
public final class ViewTensor extends Tensor {
	
	private final Tensor _underlying;
	private final int[] _startIndices;
	
	public ViewTensor(
			final Tensor underlying,
			final Location start,
			final Shape shape) {
		
		super(shape);
		
		TensorUtils.checkNdimsMatch(underlying.ndim, start.ndim);
		TensorUtils.checkNdimsMatch(underlying.ndim, shape.ndim);
		
		_underlying = underlying;
		_startIndices = start.indices;
		
		// check doesn't go outside of underlying
		final int[] thisSizes = this.shape.sizes;
		final int[] underSizes = _underlying.shape.sizes;
		for (int i=0; i<this.ndim; i++) {
			final int startIndex = _startIndices[i];
			if (startIndex < 0 || startIndex >= underSizes[i]) {
				throw new IndexOutOfBoundsException(
					"Shape start index " + startIndex + " is out " +
					"of bounds of underlying dimension of size " + 
					underSizes[i]);
			}
			final int endIndex = startIndex + thisSizes[i];
			if (endIndex < 0 || endIndex > underSizes[i]) {
				throw new IndexOutOfBoundsException(
					"Shape end index " + endIndex + " is out " +
					"of bounds of underlying dimension of size " + 
					underSizes[i]);
			}
		}
	}
	
	private int calcUnderlyingFlatIndex(final int thisFlatIndex) {
		
		if (thisFlatIndex < 0 || thisFlatIndex >= this.size) {
			throw new IndexOutOfBoundsException(
				"Flat index " + thisFlatIndex + " is out of " +
				"bounds (total size " + this.size + ")");
		}

		final int[] thisMultipliers = this.shape.multipliers;
		final int[] underMultipliers = _underlying.shape.multipliers;
		
		int underFlatIndex = 0;
		int remainder = thisFlatIndex;
		for (int i=0; i<this.ndim; i++) {
			final int thisMultiplier = thisMultipliers[i];
			final int thisIndex = remainder / thisMultiplier;
			underFlatIndex += underMultipliers[i] * (thisIndex + _startIndices[i]);
			remainder = remainder % thisMultiplier;
		}
		return underFlatIndex;
	}

	@Override
	public double get(final Location location) {
		return _underlying.get(calcUnderlyingFlatIndex(
				this.shape.calcFlatIndexFromLocation(
						location)));
	}

	@Override
	public void set(final Location location, final double value) {
		_underlying.set(calcUnderlyingFlatIndex(
				this.shape.calcFlatIndexFromLocation(
						location)), value);
	}

	@Override
	public double get(int flatIndex) {
		return _underlying.get(calcUnderlyingFlatIndex(flatIndex));
	}

	@Override
	public void set(int flatIndex, double value) {
		_underlying.set(calcUnderlyingFlatIndex(flatIndex), value);
	}

}
