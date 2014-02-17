package me.akuz.core.math.geom;

public final class Point2D {
	
	private double _x;
	private double _y;
	
	public Point2D(final double x, final double y) {
		_x = x;
		_y = y;
	}
	
	public double x() {
		return _x;
	}
	public void setX(final double x) {
		_x = x;
	}
	
	public double y() {
		return _y;
	}
	public void setY(final double y) {
		_y = y;
	}

}
