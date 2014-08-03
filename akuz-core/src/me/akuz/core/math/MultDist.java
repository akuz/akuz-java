package me.akuz.core.math;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Multinomial distribution with a symmetric Dirichlet prior.
 *
 */
public final class MultDist implements Cloneable {
	
	private final static double EPSILON = 0.0000001;
	private double _priorDirichletAlpha;
	private double[] _alloc;
	
	public MultDist(int size, double priorDirichletAlpha) {
		if (priorDirichletAlpha <= EPSILON) {
			throw new IllegalArgumentException("Prior Dirichlet alpha must be positive");
		}
		_priorDirichletAlpha = priorDirichletAlpha;
		_alloc = new double[size];
		Arrays.fill(_alloc, priorDirichletAlpha);
	}
	
	public int getSize() {
		return _alloc.length;
	}
	
	public void reset() {
		Arrays.fill(_alloc, _priorDirichletAlpha);
	}
	
	public void addObservation(int index) {
		addObservation(index, 1.0);
	}
	public void addObservation(int index, double weight) {
		_alloc[index] += weight;
	}
	
	public void removeObservation(int index) {
		removeObservation(index, 1.0);
	}
	public void removeObservation(int index, double weight) {
		_alloc[index] -= weight;
		if (_alloc[index] < _priorDirichletAlpha) {
			if (_priorDirichletAlpha - _alloc[index] < EPSILON) {
				// treat small different as a rounding error
				_alloc[index] = _priorDirichletAlpha;
			} else {
				throw new IllegalStateException("Removed more observations than was added before");
			}
		}
	}
	
	public double[] getUnnormalizedProbs() {
		return _alloc;
	}
	public double getUnnormalizedProb(int index) {
		return _alloc[index];
	}
	
	public double[] calcProbs() {
		double[] probs = _alloc.clone();
		double sum = 0.0;
		for (int i=0; i<probs.length; i++) {
			sum += _alloc[i];
		}
		for (int i=0; i<probs.length; i++) {
			probs[i] /= sum;
		}
		return probs;
	}
	
	public String toString() {
		DecimalFormat fmt = new DecimalFormat("' '0.0000;'-'0.0000");
		StringBuilder sb = new StringBuilder();
		sb.append("<MULT> alpha: ");
		sb.append(fmt.format(_priorDirichletAlpha));
		sb.append(" (");
		double[] probs = calcProbs();
		for (int i=0; i<probs.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(fmt.format(probs[i]));
		}
		sb.append(")");
		return sb.toString();
	}
	
	public MultDist clone() {
		try {
			MultDist copy = (MultDist)super.clone();
			copy._alloc = copy._alloc.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
