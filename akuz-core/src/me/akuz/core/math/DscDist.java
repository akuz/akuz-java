package me.akuz.core.math;

/**
 * N-dimensional discrete distribution.
 * Implemented as dense or sparse versions.
 *
 */
public interface DscDist {
	
	/**
	 * Get dimensionality of the discrete distribution.
	 */
	int getDim();
	
	/**
	 * Get probability of an index.
	 */
	double getProb(int index);
	
	/**
	 * Add probabilities from this distribution
	 * to the specified array, multiplying them 
	 * by the provided weight. This is method is 
	 * provided to efficient exampling of HDP
	 * from different implementations of 
	 * discrete distributions.
	 */
	void addToArray(double[] arr, double weight);

}
