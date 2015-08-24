package me.akuz.mnist.digits.hrp;

public final class Leg {
	
	private final double _centerX;
	private final double _centerY;
	private final double _size;
	
	public Leg(
			final double centerX,
			final double centerY,
			final double size) {
		
		if (size <= 0) {
			throw new IllegalArgumentException(
					"Spread leg size must be positive");
		}
		if (size > 1) {
			throw new IllegalArgumentException(
					"Spread leg size must be <= 1");
		}
		
		_centerX = centerX;
		_centerY = centerY;
		_size = size;
	}
	
	/**
	 * Center X coordinate w.r.t. the parent: [0,1].
	 */
	public double getCenterX() {
		return _centerX;
	}
	
	/**
	 * Center Y coordinate w.r.t. the parent: [0,1].
	 */
	public double getCenterY() {
		return _centerY;
	}
	
	/**
	 * Size w.r.t. the parent: (0,1].
	 */
	public double getSize() {
		return _size;
	}

}
