package me.akuz.mnist.digits.hrp;

/**
 * Spread of patch (or fractal) legs.
 */
public final class Spread {
	
	/**
	 * Defines one leg in the center, of the same size as the parent.
	 */
	public static final Spread ALTERNATE;
	
	/**
	 * Defines four legs of 0.5 size of the parent, one in each quadrant.
	 */
	public static final Spread SPATIAL;
	
	static {
		Leg[] legs = new Leg[1];
		legs[0] = new Leg(0.5, 0.5, 1.0);
		ALTERNATE = new Spread(legs);
	}
	
	static {
		Leg[] legs = new Leg[4];
		legs[0] = new Leg(0.25, 0.25, 0.5);
		legs[1] = new Leg(0.75, 0.25, 0.5);
		legs[2] = new Leg(0.25, 0.75, 0.5);
		legs[3] = new Leg(0.75, 0.75, 0.5);
		SPATIAL = new Spread(legs);
	}
	
	private final Leg[] _legs;
	
	public Spread(final Leg[] legs) {
		
		if (legs == null) {
			throw new NullPointerException("legs");
		}
		if (legs.length == 0) {
			throw new IllegalArgumentException(
					"Size of the legs array must be positive");
		}
		_legs = legs;
	}
	
	public int getLegCount() {
		return _legs.length;
	}
	
	public Leg[] getLegs() {
		return _legs;
	}

}
