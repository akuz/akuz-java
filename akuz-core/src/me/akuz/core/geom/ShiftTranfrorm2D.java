package me.akuz.core.geom;

public final class ShiftTranfrorm2D implements Transform2D {

	private final double _xShift;
	private final double _yShift;
	
	public ShiftTranfrorm2D(final double xShift, final double yShift) {
		_xShift = xShift;
		_yShift = yShift;
	}

	@Override
	public void toTarget(Point2D point) {
		point.setX(point.x() + _xShift);
		point.setY(point.y() + _yShift);
	}

	@Override
	public void toSource(Point2D point) {
		point.setX(point.x() - _xShift);
		point.setY(point.y() - _yShift);
	}
	
}
