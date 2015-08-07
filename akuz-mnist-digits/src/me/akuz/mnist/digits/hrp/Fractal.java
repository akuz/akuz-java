package me.akuz.mnist.digits.hrp;

/**
 * Image-specific analysis fractal.
 */
public final class Fractal {
	
	private final double _size;
	private final double[] _patchProbs;

	private Fractal _leg1;
	private Fractal _leg2;
	private Fractal _leg3;
	private Fractal _leg4;
	
	public Fractal(final Layer level, final double size) {
		_size = size;
		_patchProbs = new double[level.getDim()];
	}

	public double getSize() {
		return _size;
	}
	
	public double[] getPatchProbs() {
		return _patchProbs;
	}
	
	public Fractal getLeg1() {
		return _leg1;
	}
	
	public Fractal getLeg2() {
		return _leg2;
	}
	
	public Fractal getLeg3() {
		return _leg3;
	}
	
	public Fractal getLeg4() {
		return _leg4;
	}
	
	public boolean hasNextLayerDetails() {
		return _leg1 != null;
	}

	public void createNextLayerDetails(final Layer layer) {
		final double nextSize = _size / 2.0;
		_leg1 = new Fractal(layer, nextSize);
		_leg2 = new Fractal(layer, nextSize);
		_leg3 = new Fractal(layer, nextSize);
		_leg4 = new Fractal(layer, nextSize);
	}

}
