package me.akuz.core.math.geom;

public final class StretchTransform2D implements Transform2D {

	private final double _xCoeff;
	private final double _yCoeff;
	
	public StretchTransform2D(final double xCoeff, final double yCoeff) {
		if (Math.abs(xCoeff) <= Double.MIN_NORMAL) {
			throw new IllegalArgumentException("xCoeff must be positive");
		}
		if (Math.abs(yCoeff) <= Double.MIN_NORMAL) {
			throw new IllegalArgumentException("yCoeff must be positive");
		}
		_xCoeff = xCoeff;
		_yCoeff = yCoeff;
	}

	@Override
	public void toTarget(Point2D point) {
		point.setX(point.x() * _xCoeff);
		point.setY(point.y() * _yCoeff);
	}

	@Override
	public void toSource(Point2D point) {
		point.setX(point.x() / _xCoeff);
		point.setY(point.y() / _yCoeff);
	}
	
}
