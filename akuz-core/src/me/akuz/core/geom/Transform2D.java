package me.akuz.core.geom;

/**
 * Transformation of 2D points.
 *
 */
public interface Transform2D {

	/**
	 * Transform source point coordinates to target coordinates.
	 * @param point
	 */
	void toTarget(Point2D point);
	
	/**
	 * Transform target point coordinates to source coordinates.
	 * @param point
	 */
	void toSource(Point2D point);
}
