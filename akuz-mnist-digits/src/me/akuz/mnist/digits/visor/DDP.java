package me.akuz.mnist.digits.visor;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.GammaFunction;
import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;
import me.akuz.ml.tensors.TensorIterator;

/**
 * Discrete Dirichlet Process(es) along the last 
 * dimension of the specified tensor shape.
 *
 */
public final class DDP {
	
	final Shape _shape;
	final int _ndim;
	final int _lastDim;
	final int _lastDimSize;
	final int _lastDimLastIdx;
	
	final Tensor _base;
	final double _baseMass;
	final Tensor _obs;
	final Tensor _obsMass;
	final double _maxObsMass;

	public DDP(
			final Shape shape, 
			final double baseInit,
			final double baseNoise,
			final double baseMass,
			final double maxObsMass) {
		
		if (shape == null) {
			throw new NullPointerException("shape");
		}
		if (baseInit <= 0.0) {
			throw new IllegalArgumentException("baseInit must be > 0, got " + baseInit);
		}
		if (baseNoise < 0.0) {
			throw new IllegalArgumentException("baseInit must be >= 0, got " + baseNoise);
		}
		if (baseMass <= 0.0) {
			throw new IllegalArgumentException("baseMass must be > 0, got " + baseMass);
		}
		if (maxObsMass <= 0.0) {
			throw new IllegalArgumentException("maxObservedMass must be > 0, got " + maxObsMass);
		}
		
		// initialize base discrete distributions
		// using the provided baseInit and baseNoise 
		// parameters, and normalizing the last dimension:
		//
		_shape = shape;
		_ndim = shape.ndim;
		_lastDim = shape.ndim-1;
		_lastDimSize = shape.sizes[_lastDim];
		_lastDimLastIdx = _lastDimSize-1;
		_base = new DenseTensor(shape);
		_baseMass = baseMass;
		_maxObsMass = maxObsMass;
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
		_obs = new DenseTensor(shape);
		
		// observed mass shape
		final Shape obsMassShape;
		if (shape.ndim == 1) {
			obsMassShape = new Shape(1);
		} else {
			int[] obsMassIndices = Arrays.copyOf(shape.sizes, shape.ndim-1);
			obsMassShape = new Shape(obsMassIndices);
		}
		
		// observed mass tensor
		_obsMass = new DenseTensor(obsMassShape);
	}
	
	/**
	 * Add observation of the provided discrete distribution 
	 * with specified mass at the specified sub-location.
	 * 
	 * WARNING: the obsData must be the data of a
	 * tensor with the LAST dimension to sample.
	 */
	public void addObservation(
			final double mass,
			final Location subLoc,
			final double[] obsData,
			int obsDataIndex) {

		// handle root
		if (_ndim == 1) {
			_obsMass.add(0, mass);
		} else {
			_obsMass.add(subLoc, mass);
		}

		// calculate write start index
		final int [] writeIndices = Arrays.copyOf(subLoc.indices, _ndim);
		final Location writeLocation = new Location(writeIndices);
		int writeIndex = _shape.calcFlatIndexFromLocation(writeLocation);
		
		// write the data, crash on out of bounds if passed data is bad
		for (int i=0; i<_lastDimSize; i++) {
			
			_obs.add(
					writeIndex, 
					mass*obsData[obsDataIndex]);

			++obsDataIndex;
			++writeIndex;
		}
	}
	
	/**
	 * Log-likelihood of the provided discrete
	 * distribution under the posterior of this 
	 * Discrete Dirichlet Process at the 
	 * specified sub-location:
	 * 
	 * log(DP(a+n, a/(a+n)*H + n/(a+n)*Sample)),
	 * 
	 * DP(a, M) = Dir(a*M(x1), ..., a*M(xK)).
	 * 
	 * WARNING: the obsData must be the data of a
	 * tensor with the LAST dimension to sample.
	 */
	public double posteriorLogLike(
			final Location subLoc,
			final double[] obsData,
			int obsDataIndex) {

		// handle root
		final double obsMass; 
		if (_ndim == 1) {
			obsMass = _obsMass.get(0);
		} else {
			obsMass = _obsMass.get(subLoc);
		}
		
		// observations threshold
		final double adjMultiplier;
		final double adjObsMass;
		if (obsMass > _maxObsMass) {

			// discount observed sums by overshoot
			adjMultiplier = _maxObsMass / obsMass;
			adjObsMass = _maxObsMass;
			
		} else {
			
			adjMultiplier = 1.0;
			adjObsMass = obsMass;
		}
		
		// calculate read start index
		final int [] readIndices = Arrays.copyOf(subLoc.indices, _ndim);
		final Location readLocation = new Location(readIndices);
		int readIndex = _shape.calcFlatIndexFromLocation(readLocation);
		
		// calculate posterior dirichlet alpha
		final double a = _baseMass;
		final double n = adjObsMass;
		final double a_plus_n = a + n;
		final double posterior_dp_alpha = a_plus_n;
		
		// accumulate log-likelihood
		double logLike = 0.0;

		// sum up posterior dirichlet alphas
		double sumPosteriorDirAlpha = 0.0;
		
		// read the data, crash on out of bounds if passed data is bad
		for (int i=0; i<_lastDimSize; i++) {
			
			// read the values at index
			final double baseIdxMass = _base.get(readIndex);
			final double adjObsIdxMass = adjMultiplier*_obs.get(readIndex);
			final double obsValue = obsData[obsDataIndex];
			
			final double posterior_dp_mass =
					a / a_plus_n * baseIdxMass +
					n / a_plus_n * adjObsIdxMass;
			
			final double posteriorDirAlpha =
					posterior_dp_alpha *
					posterior_dp_mass;
			
			// accumulate results
			logLike -= GammaFunction.lnGamma(posteriorDirAlpha);
			logLike += (posteriorDirAlpha - 1.0) * Math.log(obsValue);
			sumPosteriorDirAlpha += posteriorDirAlpha;
			
			++obsDataIndex;
			++readIndex;
		}
		
		// finish off the normalization constant
		logLike += GammaFunction.lnGamma(sumPosteriorDirAlpha);
		
		return logLike;
	}

}
