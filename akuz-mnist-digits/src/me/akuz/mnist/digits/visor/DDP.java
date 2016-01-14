package me.akuz.mnist.digits.visor;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;
import me.akuz.ml.tensors.TensorIterator;

/**
 * Discrete Dirichlet Process.
 *
 */
public final class DDP {
	
	public DDP(
			final Shape shape, 
			final double baseInit,
			final double baseNoise,
			final double baseWeight,
			final double maxObsWeight) {
		
		if (shape == null) {
			throw new NullPointerException("shape");
		}
		if (baseInit <= 0.0) {
			throw new IllegalArgumentException("baseInit must be > 0, got " + baseInit);
		}
		if (baseNoise < 0.0) {
			throw new IllegalArgumentException("baseInit must be >= 0, got " + baseNoise);
		}
		
		// initialize base discrete distributions
		// using the provided baseInit and baseNoise 
		// parameters, and normalizing the last dimension:
		//
		final int lastDim = shape.ndim-1;
		final int lastDimIdx = shape.sizes[lastDim]-1;
		final Tensor base = new DenseTensor(shape);
		final TensorIterator it = new TensorIterator(base.shape);
		final Random rnd = ThreadLocalRandom.current();
		double lastDimSum = 0.0;
		while (it.next()) {
			
			// get current location
			final Location loc = it.loc();
			final int[] indices = loc.indices;
			
			// generate base distribution value
			final double value = baseInit + rnd.nextDouble()*baseNoise;
			
			// set and add to sum
			base.set(loc, value);
			lastDimSum += value;
			
			// normalize the last dimension
			if (indices[lastDim] == lastDimIdx) {
				
				if (lastDimSum <= 0.0) {
					throw new IllegalStateException(
						"Sum across last dimension must be > 0, " + 
						"but got " + lastDimSum);
				}
				
				// re-loop last dimension
				final double normalizer = 1.0 / lastDimSum;
				for (int i=0; i<=lastDimIdx; i++) {
					indices[lastDim] = i;
					base.mul(loc, normalizer);
				}
				
				// reset last dimension sum
				lastDimSum = 0.0;
			}
		}
		
	}
	
	/**
	 * Add observation of a discrete distribution 
	 * at the specified sub-location.
	 */
	public void observe(final Location subLoc, final Tensor dist, final double weight) {
		
		// TODO: accept slice instead of tensor
		
		// TODO
	}
	
	/**
	 * Posterior log-likelihood of the provided discrete
	 * distribution under the Dirichlet Process at the
	 * specified sub-location.
	 */
	public double logLike(final Location subLoc, final Tensor dist) {

		// TODO: accept slice instead of tensor

		// TODO
		
		return Double.NaN;
	}

}
