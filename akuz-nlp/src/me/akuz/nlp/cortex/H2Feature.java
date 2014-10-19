package me.akuz.nlp.cortex;

import me.akuz.core.math.DirDist;

public final class H2Feature {

	private final DirDist _left;
	private final DirDist _right;
	
	public H2Feature(
			final int legDim, 
			final double legAlpha) {
		
		_left = new DirDist(legDim, legAlpha);
		_right = new DirDist(legDim, legAlpha);
	}
	
	public DirDist getLeft() {
		return _left;
	}
	
	public DirDist getRight() {
		return _right;
	}
	
	public void normalize() {
		_left.normalize();
		_right.normalize();
	}
	
}
