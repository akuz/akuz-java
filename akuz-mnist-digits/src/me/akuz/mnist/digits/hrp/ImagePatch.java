package me.akuz.mnist.digits.hrp;

import me.akuz.mnist.digits.hrp.prob.Gauss;
import me.akuz.mnist.digits.hrp.prob.GaussKnownAll;
import me.akuz.mnist.digits.hrp.prob.GaussOvertakeMean;

/**
 * Patch of a specific image analysis fractal.
 *
 */
public final class ImagePatch {
	
	private final double _size;
	private final Gauss _addX;
	private final Gauss _addY;
	private final Gauss _X;
	private final Gauss _Y;
	
//	private Leg _leg1;
//	private Leg _leg2;
//	private Leg _leg3;
//	private Leg _leg4;
	
	public ImagePatch(
			final double size,
			final Gauss X,
			final Gauss Y) {
		_size = size;
		_addX = X;
		_addY = Y;
		_X = X;
		_Y = Y;
	}

	public ImagePatch(
			final ImagePatch parent,
			final boolean shiftX,
			final boolean shiftY) {

		_size = parent.getSize() / 2.0;

		_addX = new GaussKnownAll(
				null, 
				shiftX ? _size / 2.0 : -_size / 2.0,
				1.0 / Math.pow(_size * Const.PATCH_JIGGLE, 2));

		_addY = new GaussKnownAll(
				null, 
				shiftY ? _size / 2.0 : -_size / 2.0,
				1.0 / Math.pow(_size * Const.PATCH_JIGGLE, 2));
		
		_X = new GaussOvertakeMean(
				null, 
				parent.getX(), 
				_addX);
		
		_Y = new GaussOvertakeMean(
				null, 
				parent.getY(), 
				_addY);
	}
	
	public double getSize() {
		return _size;
	}
	
	public Gauss getX() {
		return _X;
	}
	
	public Gauss getY() {
		return _Y;
	}

}
