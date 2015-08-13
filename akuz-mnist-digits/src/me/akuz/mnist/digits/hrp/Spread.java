package me.akuz.mnist.digits.hrp;

/**
 * Spread of patch (or fractal) legs.
 */
public final class Spread {
	
	/**
	 * Defines one leg in the center, of the same size as the parent.
	 */
	public static final Spread CENTRAL;
	
	/**
	 * Defines four legs of 0.5 size of the parent, one in each quadrant.
	 */
	public static final Spread SPATIAL;
	
	static {
		SpreadLeg[] legs = new SpreadLeg[1];
		legs[0] = new SpreadLeg(0.5, 0.5, 1.0);
		CENTRAL = new Spread(legs);
	}
	
	static {
		SpreadLeg[] legs = new SpreadLeg[4];
		legs[0] = new SpreadLeg(0.25, 0.25, 0.5);
		legs[1] = new SpreadLeg(0.75, 0.25, 0.5);
		legs[2] = new SpreadLeg(0.25, 0.75, 0.5);
		legs[3] = new SpreadLeg(0.75, 0.75, 0.5);
		SPATIAL = new Spread(legs);
	}
	
	private final SpreadLeg[] _legs;
	
	public Spread(final SpreadLeg[] legs) {
		
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
	
	public SpreadLeg[] getLegs() {
		return _legs;
	}

}
