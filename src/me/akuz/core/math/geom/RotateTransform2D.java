package me.akuz.core.math.geom;

public final class RotateTransform2D implements Transform2D {

	private final double _rad;
	private final double _cos;
	private final double _sin;
	
	public RotateTransform2D(final double rad) {
		_rad = rad;
		_cos = Math.cos(rad);
		_sin = Math.sin(rad);
	}
	
	public double getRad() {
		return _rad;
	}
	
	@Override
	public void toTarget(Point2D point) {
		final double xNew = + point.x() * _cos - point.y() * _sin;
		final double yNew = + point.x() * _sin + point.y() * _cos;
		point.setX(xNew);
		point.setY(yNew);
	}

	@Override
	public void toSource(Point2D point) {
		// change sinus sign, cosinus stays the same
		final double xNew = + point.x() * _cos + point.y() * _sin;
		final double yNew = - point.x() * _sin + point.y() * _cos;
		point.setX(xNew);
		point.setY(yNew);
	}

}
