package me.akuz.fb.loc;

import me.akuz.core.math.NIGDist;

public final class Place {
	
	private final Long _id;
	private final NIGDist _xDist;
	private final NIGDist _yDist;
	
	public Place(final Long id) {
		_id = id;
		_xDist = new NIGDist(0.0, 1.0, 1.0, 1.0);
		_yDist = new NIGDist(0.0, 1.0, 1.0, 1.0);
	}
	
	public Long getId() {
		return _id;
	}
	
	public NIGDist getXDist() {
		return _xDist;
	}
	
	public NIGDist getYDist() {
		return _yDist;
	}
	
	public void addObservation(double x, double y, double accuracy) {
		_xDist.addObservation(x, accuracy);
		_yDist.addObservation(y, accuracy);
	}
	
	public double logLike(double x, double y, double accuracy) {
		return _xDist.logLike(x) + _yDist.logLike(y);
	}

}
