package me.akuz.mnist.digits.hrp;

import me.akuz.mnist.digits.hrp.prob.Gauss;
import me.akuz.mnist.digits.hrp.prob.GaussKnownAll;
import me.akuz.mnist.digits.hrp.prob.GaussOvertakeMean;

public final class Leg {
	
	public static double LEG_JIGGLE = 0.1;
	
	private final String _name;
	private final double _size;
	private final Gauss _addX;
	private final Gauss _addY;
	private final Gauss _legX;
	private final Gauss _legY;
	
	public Leg(
			final String name,
			final double size,
			final Gauss addX,
			final Gauss addY) {
		_name = name;
		_size = size;
		_addX = addX;
		_addY = addY;
		_legX = addX;
		_legY = addY;
	}
	
	public Leg(
			final Leg parent,
			final boolean shiftX,
			final boolean shiftY) {
		
		_name = parent.getName() + "_" +
				(shiftX ? "r" : "l") + 
				(shiftY ? "b" : "t");

		_size = parent.getSize() / 2.0;

		_addX = new GaussKnownAll(
				_name + "_addX", 
				shiftX ? _size / 2.0 : -_size / 2.0,
				1.0 / Math.pow(_size * LEG_JIGGLE, 2));

		_addY = new GaussKnownAll(
				_name + "_addY", 
				shiftY ? _size / 2.0 : -_size / 2.0,
				1.0 / Math.pow(_size * LEG_JIGGLE, 2));
		
		_legX = new GaussOvertakeMean(
				_name + "_legX", 
				parent.getLegX(), 
				_addX);
		
		_legY = new GaussOvertakeMean(
				_name + "_legY", 
				parent.getLegY(), 
				_addY);
	}
	
	public String getName() {
		return _name;
	}
	
	public double getSize() {
		return _size;
	}
	
	public Gauss getLegX() {
		return _legX;
	}
	
	public Gauss getLegY() {
		return _legY;
	}

}
