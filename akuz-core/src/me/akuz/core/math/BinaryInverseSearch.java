package me.akuz.core.math;

import java.security.InvalidParameterException;

/**
 * Binary search algorithm for solving
 * an inverse problem of finding x within [xmin, xmax],
 * such that f(x) = ytarget, for a monotone function f(x).
 *
 */
public final class BinaryInverseSearch {
	
	private double _x;
	
	public BinaryInverseSearch(
			SingleArgFunction f, 
			double minX, 
			double maxX, 
			double targetY, 
			int maxIterations,
			double stopPrecision) {
		
		if (minX >= maxX) {
			throw new InvalidParameterException("minX should be < maxX");
		}
		
		// check min X
		double minY = f.getValueAt(minX);
		if (minY > targetY) {
			throw new InvalidParameterException("targetY is out of scope");
		}
		if (Math.abs(minY-targetY) < stopPrecision) {
			_x = minX;
			return;
		}

		// check min Y
		double maxY = f.getValueAt(maxX);
		if (maxY < targetY) {
			throw new InvalidParameterException("targetY is out of scope");
		}
		if (Math.abs(maxY-targetY) < stopPrecision) {
			_x = maxX;
			return;
		}
		
		int iterations = 0;
		while (true) {
			
			++iterations;
			
			double x = 0.5*minX + 0.5*maxX;
			
			double y = f.getValueAt(x);
			
			if (Math.abs(y-targetY) < stopPrecision) {
				_x = x;
				return;
			}
			
			if (y < targetY) {
				minX = x;
			} else {
				maxX = x;
			}
		
			if (iterations >= maxIterations) {
				throw new IllegalStateException("Could not find solution in " + maxIterations + " iterations");
			}
		}
	}

	public final double getX() {
		return _x;
	}
}
