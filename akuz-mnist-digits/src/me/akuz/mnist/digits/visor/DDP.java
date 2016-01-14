package me.akuz.mnist.digits.visor;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;
import me.akuz.ml.tensors.TensorIterator;
import me.akuz.ml.tensors.ViewTensor;

/**
 * Discrete Dirichlet Process(es) along the last 
 * dimension of the specified tensor shape.
 *
 */
public final class DDP {
	
	final int _ndim;
	final int _lastDim;
	final int _lastDimSize;
	final int _lastDimLastIdx;
	
	final Tensor _base;
	final Tensor _observed;
	final Tensor _observedMass;

	public DDP(
			final Shape shape, 
			final double baseInit,
			final double baseNoise,
			final double baseDistMass,
			final double maxObservedMass) {
		
		if (shape == null) {
			throw new NullPointerException("shape");
		}
		if (baseInit <= 0.0) {
			throw new IllegalArgumentException("baseInit must be > 0, got " + baseInit);
		}
		if (baseNoise < 0.0) {
			throw new IllegalArgumentException("baseInit must be >= 0, got " + baseNoise);
		}
		if (baseDistMass <= 0.0) {
			throw new IllegalArgumentException("baseDistMass must be > 0, got " + baseDistMass);
		}
		if (maxObservedMass <= 0.0) {
			throw new IllegalArgumentException("maxObservedMass must be > 0, got " + maxObservedMass);
		}
		
		// initialize base discrete distributions
		// using the provided baseInit and baseNoise 
		// parameters, and normalizing the last dimension:
		//
		_ndim = shape.ndim;
		_lastDim = shape.ndim-1;
		_lastDimSize = shape.sizes[_lastDim];
		_lastDimLastIdx = _lastDimSize-1;
		_base = new DenseTensor(shape);
		final TensorIterator it = new TensorIterator(_base.shape);
		final Random rnd = ThreadLocalRandom.current();
		double lastDimSum = 0.0;
		while (it.next()) {
			
			// get current location
			final Location loc = it.loc();
			final int[] indices = loc.indices;
			
			// generate base distribution value
			final double value = baseInit + rnd.nextDouble()*baseNoise;
			
			// set and add to sum
			_base.set(loc, value);
			lastDimSum += value;
			
			// normalize the last dimension
			if (indices[_lastDim] == _lastDimLastIdx) {
				
				if (lastDimSum <= 0.0) {
					throw new IllegalStateException(
						"Sum across last dimension must be > 0, " + 
						"but got " + lastDimSum);
				}
				
				// re-loop last dimension
				final double normalizer = 1.0 / lastDimSum;
				for (int i=0; i<=_lastDimLastIdx; i++) {
					indices[_lastDim] = i;
					_base.mul(loc, normalizer);
				}
				
				// reset last dimension sum
				lastDimSum = 0.0;
			}
		}
		
		// observed distributions sum
		_observed = new DenseTensor(shape);
		
		// observed mass shape
		final Shape obsMassShape;
		if (shape.ndim == 1) {
			obsMassShape = new Shape(1);
		} else {
			int[] obsMassIndices = Arrays.copyOf(shape.sizes, shape.ndim-1);
			obsMassShape = new Shape(obsMassIndices);
		}
		
		// observed mass tensor
		_observedMass = new DenseTensor(obsMassShape);
	}
	
	/**
	 * Add observation of the provided discrete distribution 
	 * with specified mass at the specified sub-location.
	 */
	public void addObservation(final Location subLoc, final Tensor dist, final double mass) {

		if (dist.size != _lastDimSize) {
			throw new IllegalArgumentException(
				"Observed distribution must be of size " + _lastDimSize + 
				", but got the shape " + dist.shape + " of size " + dist.size);
		}
		
		_observedMass.add(subLoc, mass);
		
		// TODO: root case (null subLoc)
		// TODO: performance improvement
		final int[] startIndices = new int[_ndim];
		for (int i=0; i<_lastDim; i++) {
			startIndices[i] = subLoc.indices[i];
		}
		final Location start = new Location(startIndices);
		final int[] sizes = new int[_ndim];
		for (int i=0; i<_lastDim; i++) {
			sizes[i] = 1;
		}
		sizes[_lastDim] = _lastDimSize;
		final Shape shape = new Shape(sizes);
		final Tensor view = new ViewTensor(_observed, start, shape);
		for (int i=0; i<view.size; i++) {
			view.add(i, dist.get(i) * mass);
		}
	}
	
	/**
	 * Log-likelihood of the provided discrete
	 * distribution under the posterior of this 
	 * Discrete Dirichlet Process at the 
	 * specified sub-location.
	 */
	public double posteriorLogLike(final Location subLoc, final Tensor dist) {

		if (dist.size != _lastDimSize) {
			throw new IllegalArgumentException(
				"Observed distribution must be of size " + _lastDimSize + 
				", but got the shape " + dist.shape + " of size " + dist.size);
		}

		// TODO
		
		return Double.NaN;
	}

}
