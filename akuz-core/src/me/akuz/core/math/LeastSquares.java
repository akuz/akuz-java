package me.akuz.core.math;

import Jama.Matrix;

/**
 * Least-squares solutions for various fitting problems.
 *
 */
public final class LeastSquares {

	/**
	 * Find linear least-squares approximation.
	 * Solve linear problem A*x=b for x, where rows of A and b:
	 * A = [1, x]
	 * b = [y]
	 * @param mXY - Two column input matrix with a pair [x, y] in each row.
	 * @return Column of three coefficients: for 1, x
	 */
	public static final Matrix solveLinear(Matrix mXY) {
		
		// prepare matrices A and b
		Matrix A = new Matrix(mXY.getRowDimension(), 2);
		Matrix b = new Matrix(mXY.getRowDimension(), 1);
		for (int i=0; i<mXY.getRowDimension(); i++) {
			
			double x = mXY.get(i, 0);
			double y = mXY.get(i, 1);
			
			A.set(i, 0, 1);
			A.set(i, 1, x);
			b.set(i, 0, y);
		}
		
		// solve for x
		Matrix x = A.solve(b);
		return x;
	}

	/**
	 * Find quadratic least-squares approximation.
	 * Solve linear problem A*x=b for x, where rows of A and b:
	 * A = [1, x, x^2]
	 * b = [y]
	 * @param mXY - Two column input matrix with a pair [x, y] in each row.
	 * @return Column of three coefficients: for 1, x and x^2
	 */
	public static final Matrix solveQuadratic(Matrix mXY) {
		
		// prepare matrices A and b
		Matrix A = new Matrix(mXY.getRowDimension(), 3);
		Matrix b = new Matrix(mXY.getRowDimension(), 1);
		for (int i=0; i<mXY.getRowDimension(); i++) {
			
			double x = mXY.get(i, 0);
			double y = mXY.get(i, 1);
			
			A.set(i, 0, 1);
			A.set(i, 1, x);
			A.set(i, 2, x*x);
			b.set(i, 0, y);
		}
		
		// solve for x
		Matrix x = A.solve(b);
		return x;
	}
	
	/**
	 * Find quadratic least-squares approximation with no intercept.
	 * Solve linear problem A*x=b for x, where rows of A and b:
	 * A = [x, x^2]
	 * b = [y]
	 * @param mXY - Two column input matrix with a pair [x, y] in each row.
	 * @return Column of two coefficients: for x and x^2
	 */
	public static final Matrix solveQuadraticNoIntercept(Matrix mXY) {
		
		// prepare matrices A and b
		Matrix A = new Matrix(mXY.getRowDimension(), 2);
		Matrix b = new Matrix(mXY.getRowDimension(), 1);
		for (int i=0; i<mXY.getRowDimension(); i++) {
			
			double x = mXY.get(i, 0);
			double y = mXY.get(i, 1);
			
			A.set(i, 0, x);
			A.set(i, 1, x*x);
			b.set(i, 0, y);
		}
		
		// solve for x
		Matrix x = A.solve(b);
		return x;
	}
	
}
