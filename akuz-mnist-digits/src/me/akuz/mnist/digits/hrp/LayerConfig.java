package me.akuz.mnist.digits.hrp;

public final class LayerConfig {
	
	private final int _dim;
	private final Spread _spread;
	
	public LayerConfig(final int dim, final Spread spread) {
		
		if (dim < 2) {
			throw new IllegalArgumentException(
					"Layer must have dim >= 2, got " + dim);
		}
		if (spread == null) {
			throw new NullPointerException("spread");
		}
		_dim = dim;
		_spread = spread;
	}
	
	public int getDim() {
		return _dim;
	}
	
	public Spread getSpread() {
		return _spread;
	}

}
