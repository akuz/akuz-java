package me.akuz.mnist.digits.tensor;

public final class TensorView extends Tensor {
	
	private final Tensor _underlying;
	private final int[] _startIndices;
	private final int[] _shapeSizes;
	
	public TensorView(
			final Tensor underlying,
			final Location startLoc,
			final Shape shape) {
		
		super(shape);
		
		UtilsForTensors.checkNdimsMatch(underlying.ndim, startLoc.ndim);
		UtilsForTensors.checkNdimsMatch(underlying.ndim, shape.ndim);
		
		_underlying = underlying;
		_startIndices = startLoc.indices;
		_shapeSizes = shape.sizes;
		
		// TODO: check the view doesn't go outside underlying
	}
	
	private Location calculateUnderlyingLocation(final Location location) {

		UtilsForTensors.checkNdimsMatch(_underlying.ndim, location.ndim);
		
		final int[] locationIndices = location.indices;
		final int[] underlyingIndices = new int[_startIndices.length];
		for (int i=0; i<underlyingIndices.length; i++) {
			
			final int locationIndex = locationIndices[i];

			if (locationIndex < 0 || locationIndex >= _shapeSizes[i]) {
				throw new IndexOutOfBoundsException(
					"Index " + locationIndex + " is out of bounds [0," +
					_shapeSizes[i] + ") in dimension " + i);
			}
			
			underlyingIndices[i] = _startIndices[i] + locationIndex;
		}
		
		return new Location(underlyingIndices);
	}

	@Override
	public double get(final Location location) {
		
		// TODO: add location caching...

		return _underlying.get(calculateUnderlyingLocation(location));
	}

	@Override
	public void set(final Location location, final double value) {
		
		// TODO: add location caching...

		_underlying.set(calculateUnderlyingLocation(location), value);
	}

}
