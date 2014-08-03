package me.akuz.core.math;

import Jama.Matrix;

/**
 * Sparse overlay matrix on top of a dense matrix.
 *
 */
public final class OverlayMatrix {
	
	private final Matrix _base;
	private final double _baseMultiplier;
	private final SparseMatrix<Integer, Double> _sparseOverlay;
	
	public OverlayMatrix(Matrix base, double baseMultiplier) {
		_base = base;
		_baseMultiplier = baseMultiplier;
		_sparseOverlay = new SparseMatrix<>();
	}
	
	public double get(Integer i, Integer j) {
		SparseVector<Integer, Double> vector = _sparseOverlay.get(i);
		if (vector != null) {
			Double value = vector.get(j);
			if (value != null) {
				return _base.get(i, j) * _baseMultiplier + value.doubleValue();
			} else {
				return _base.get(i, j) * _baseMultiplier;
			}
		} else {
			return _base.get(i, j) * _baseMultiplier;
		}
	}
	
	public Double getOverlay(Integer i, Integer j) {
		SparseVector<Integer, Double> vector = _sparseOverlay.get(i);
		if (vector != null) {
			return vector.get(j);
		} else {
			return null;
		}
	}
	
	public void addOverlay(Integer i, Integer j, Double value) {
		SparseVector<Integer, Double> vector = _sparseOverlay.get(i);
		if (vector == null) {
			vector = new SparseVector<>();
			_sparseOverlay.set(i, vector);
		}
		Double currValue = vector.get(j);
		if (currValue != null) {
			vector.set(j, currValue + value);
		} else {
			vector.set(j, value);
		}
	}
	
	public void setOverlay(Integer i, Integer j, Double value) {
		SparseVector<Integer, Double> vector = _sparseOverlay.get(i);
		if (vector == null) {
			vector = new SparseVector<>();
			_sparseOverlay.set(i, vector);
		}
		vector.set(j, value);
	}
	
	/**
	 * Sparse clear (preserving all allocated space).
	 */
	public void clear() {
		for (int idx=0; idx<_sparseOverlay.size(); idx++) {
			SparseVector<Integer, Double> vector = _sparseOverlay.getValueByIndex(idx);
			vector.clear();
		}
	}

}
