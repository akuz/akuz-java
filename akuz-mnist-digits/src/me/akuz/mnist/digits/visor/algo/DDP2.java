package me.akuz.mnist.digits.visor.algo;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.GammaFunction;
import me.akuz.ml.tensors.Tensor;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.TensorBase;
import me.akuz.ml.tensors.TensorIterator;

/**
 * Discrete Dirichlet Process(es) along the last 
 * dimension of the specified tensor shape.
 *
 */
public final class DDP2 {
	
	final Shape _shape;
	final int _ndim;
	final int _lastDim;
	final int _lastDimSize;
	final int _lastDimLastIdx;
	
	final TensorBase _base;
	final TensorBase _obs;
	final TensorBase _obsMass;
	double _temperature;
	double _contrast;

	public DDP2(
			final Shape shape, 
			final double baseInit,
			final double baseNoise,
			final double startTemperature,
			final double startContrast) {

		if (shape == null) {
			throw new NullPointerException("shape");
		}
		if (baseInit <= 0.0) {
			throw new IllegalArgumentException("baseInit must be > 0, got " + baseInit);
		}
		if (baseNoise < 0.0) {
			throw new IllegalArgumentException("baseInit must be >= 0, got " + baseNoise);
		}
		setTemperature(startTemperature);
		setContrast(startContrast);
		
		// initialize base discrete distributions
		// using the provided baseInit and baseNoise 
		// parameters, and normalizing the last dimension:
		//
		_shape = shape;
		_ndim = shape.ndim;
		_lastDim = shape.ndim-1;
		_lastDimSize = shape.sizes[_lastDim];
		_lastDimLastIdx = _lastDimSize-1;
		_base = new Tensor(shape);
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
		_obs = new Tensor(shape);
		
		// observed mass shape
		final Shape obsMassShape;
		if (shape.ndim == 1) {
			obsMassShape = new Shape(1);
		} else {
			int[] obsMassIndices = Arrays.copyOf(shape.sizes, shape.ndim-1);
			obsMassShape = new Shape(obsMassIndices);
		}
		
		// observed mass tensor
		_obsMass = new Tensor(obsMassShape);
	}

	/**
	 * Get temperature.
	 */
	public double getTemperature() {
		return _temperature;
	}
	
	/**
	 * Set temperature.
	 */
	public void setTemperature(final double temperature) {
		if (temperature <= 0.0 || temperature >= 1.0) {
			throw new IllegalArgumentException(
					"Temperature must be within the open " + 
					"interval (0.0,1.0), got " + temperature);
		}
		_temperature = temperature;
	}

	/**
	 * Get contrast.
	 */
	public double getContrast() {
		return _contrast;
	}
	
	/**
	 * Set contrast.
	 */
	public void setContrast(final double contrast) {
		if (contrast <= 0.0) {
			throw new IllegalArgumentException(
					"Contrast must be > 0, got " + 
					contrast);
		}
		_contrast = contrast;
	}
	
	/**
	 * Clear all observations.
	 */
	public void clearObservations() {
		_obsMass.fill(0.0);
		_obs.fill(0.0);
	}
	
	/**
	 * Add observation of the provided discrete distribution 
	 * with specified mass at the specified sub-location.
	 * 
	 * WARNING: the data array must be the data of a
	 * tensor with the LAST dimension to observe.
	 */
	public void addObservation(
			final double mass,
			final Location subLoc,
			final double[] data,
			int dataIndex) {

		// handle root
		if (_ndim == 1) {
			_obsMass.add(0, mass);
		} else {
			_obsMass.add(subLoc, mass);
		}

		// calculate write start index
		final int[] writeIndices;
		if (_ndim == 1) {
			writeIndices = new int[1];
		} else {
			writeIndices = Arrays.copyOf(subLoc.indices, _ndim);
		}
		final Location writeLocation = new Location(writeIndices);
		int writeIndex = _shape.calcFlatIndexFromLocation(writeLocation);
		
		// write the data, crash on out of bounds if passed data is bad
		for (int i=0; i<_lastDimSize; i++) {
			
			final double prob = data[dataIndex];
			
			if (prob < 0.0 || prob > 1.0) {
				throw new IllegalStateException("prob " + prob);
			}

			_obs.add(
					writeIndex, 
					mass*prob);

			++dataIndex;
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
	 * WARNING: the data array must be the data of a
	 * tensor with the LAST dimension to observe.
	 */
	public double calcPosteriorLogLike(
			final Location subLoc,
			final double[] data,
			int dataIndex) {

		// handle root
		final double obsMass; 
		if (_ndim == 1) {
			obsMass = _obsMass.get(0);
		} else {
			obsMass = _obsMass.get(subLoc);
		}
		
		// calculate read start index
		final int[] readIndices;
		if (_ndim == 1) {
			readIndices = new int[1];
		} else {
			readIndices = Arrays.copyOf(subLoc.indices, _ndim);
		}
		final Location readLocation = new Location(readIndices);
		int readIndex = _shape.calcFlatIndexFromLocation(readLocation);
		
		// calculate posterior dirichlet alpha
		final double a = _temperature;
		final double n = (1.0 - _temperature);
		final double a_plus_n = a + n; 
		final double posteriorDP_alpha = a_plus_n * Math.pow(_contrast, Math.pow(_lastDimSize, 2));
		
		// accumulate log-likelihood
		double logLike = 0.0;

		// sum up posterior dirichlet alphas
		double sumPosteriorDirAlpha = 0.0;
		
		// read the data, crash on out of bounds if passed data is bad
		for (int i=0; i<_lastDimSize; i++) {
			
			// read the values at index
			final double base_idxProb = _base.get(readIndex);
			final double obs_idxProb = obsMass > 0 ? _obs.get(readIndex)/obsMass : 0.0;
			final double obsValue = data[dataIndex];
			
			final double posteriorDP_idxProb =
					a / a_plus_n * base_idxProb +
					n / a_plus_n * obs_idxProb;
			
			final double posteriorDir_idxAlpha =
					posteriorDP_alpha *
					posteriorDP_idxProb;
			
			// accumulate results
			logLike += (posteriorDir_idxAlpha - 1.0) * Math.log(obsValue);
			logLike -= GammaFunction.lnGamma(posteriorDir_idxAlpha);
			sumPosteriorDirAlpha += posteriorDir_idxAlpha;
			
			++dataIndex;
			++readIndex;
		}
		
		// finish off the normalization constant
		logLike += GammaFunction.lnGamma(sumPosteriorDirAlpha);
		
		return logLike;
	}

	/**
	 * Fill posterior mean into the provided output array
	 * starting at the specified index in that array.
	 *
	 * WARNING: the data array must be the data of a
	 * tensor with the LAST dimension to write to.
	 */
	public void fillPosteriorMean(
			final Location subLoc,
			final double[] data,
			int dataIndex) {

		// handle root
		final double obsMass; 
		if (_ndim == 1) {
			obsMass = _obsMass.get(0);
		} else {
			obsMass = _obsMass.get(subLoc);
		}
		
		// calculate read start index
		final int[] readIndices;
		if (_ndim == 1) {
			readIndices = new int[1];
		} else {
			readIndices = Arrays.copyOf(subLoc.indices, _ndim);
		}
		final Location readLocation = new Location(readIndices);
		int readIndex = _shape.calcFlatIndexFromLocation(readLocation);
		
		// calculate posterior dirichlet alpha
		final double a = _temperature;
		final double n = (1.0 - _temperature);
		final double a_plus_n = a + n;
		
		// read the data, crash on out of bounds if passed data is bad
		for (int i=0; i<_lastDimSize; i++) {
			
			// read the values at index
			final double base_idxProb = _base.get(readIndex);
			final double obs_idxProb = obsMass > 0 ? _obs.get(readIndex)/obsMass : 0.0;
			
			final double posteriorDP_idxProb =
					a / a_plus_n * base_idxProb +
					n / a_plus_n * obs_idxProb;
			
			data[dataIndex] = posteriorDP_idxProb;
			
			++dataIndex;
			++readIndex;
		}
	}

}
