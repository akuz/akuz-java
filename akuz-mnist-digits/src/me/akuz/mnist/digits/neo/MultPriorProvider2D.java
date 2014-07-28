package me.akuz.mnist.digits.neo;

/**
 * Provides multinomial priors for a 2D mesh.
 *
 */
public interface MultPriorProvider2D {
	
	/**
	 * Returns number of rows of the 2D mesh.
	 */
	int getPriorRowNum();
	
	/**
	 * Returns number of columns of the 2D mesh.
	 */
	int getPriorColNum();

	/**
	 * Returns dimensionality of multinomial priors.
	 */
	int getPriorMultDim();
	
	/**
	 * Returns multinomial prior at a specific position in the 2D mesh.
	 */
	double[] getPriorMultDist(final int i, final int j);
	
}
