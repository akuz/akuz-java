package me.akuz.core.math;

import java.util.Arrays;

import Jama.Matrix;

/**
 * Diagonal matrix.
 *
 */
public final class DiagMatrix {

	private final double[] _diag;
	
	public DiagMatrix(int size) {
		_diag = new double[size];
	}
	
	public DiagMatrix(int size, double diag) {
		_diag = new double[size];
		Arrays.fill(_diag, diag);
	}
	
	public DiagMatrix(Matrix m) {
		if (m.getRowDimension() != m.getColumnDimension()) {
			throw new IllegalArgumentException("Cannot get diagonal matrix from non-square matrix");
		}
		_diag = new double[m.getRowDimension()];
		for (int k=0; k<m.getRowDimension(); k++) {
			_diag[k] = m.get(k, k);
		}
	}
	
	public int getSize() {
		return _diag.length;
	}
	
	public double[] getDiagArray() {
		return _diag;
	}
	
	public Matrix getDiagAsColumn() {
		return MatrixUtils.arrayAsColumn(_diag);
	}
	
	public double getDiag(int k) {
		return _diag[k];
	}
	
	public void setDiag(int k, double value) {
		_diag[k] = value;
	}
	
	public double get(int i, int j) {
		if (i < 0 || j < 0 || i >= _diag.length || j >= _diag.length) {
			throw new IndexOutOfBoundsException();
		}
		return (i == j) ? _diag[i] : 0.0;
	}
	
	public void set(int i, int j, double value) {
		if (i < 0 || j < 0 || i >= _diag.length || j >= _diag.length) {
			throw new IndexOutOfBoundsException();
		}
		if (i != j) {
			throw new IllegalArgumentException("Cannot set non-diagonal entries in diaginal matrix");
		}
		_diag[i] = value;
	}
	
	public int getRowDimension() {
		return _diag.length;
	}
	
	public int getColumnDimension() {
		return _diag.length;
	}
	
	public DiagMatrix inverse() {
		DiagMatrix inv = new DiagMatrix(_diag.length);
		for (int k=0; k<_diag.length; k++) {
			inv.setDiag(k, 1.0 / getDiag(k));
		}
		return inv;
	}
	
	public Matrix timesOnRight(Matrix m) {
		if (m.getRowDimension() != _diag.length) {
			throw new IllegalArgumentException("Inconsistent matrix dimensions");
		}
		Matrix res = new Matrix(_diag.length, m.getColumnDimension());
		for (int i=0; i<res.getRowDimension(); i++) {
			for (int j=0; j<res.getColumnDimension(); j++) {
				res.set(i, j, _diag[i] * m.get(i, j));
			}
		}
		return res;
	}
	
	public Matrix timesOnLeft(Matrix m) {
		if (m.getColumnDimension() != _diag.length) {
			throw new IllegalArgumentException("Inconsistent matrix dimensions");
		}
		Matrix res = new Matrix(m.getRowDimension(), _diag.length);
		for (int i=0; i<res.getRowDimension(); i++) {
			for (int j=0; j<res.getColumnDimension(); j++) {
				res.set(i, j, m.get(i, j) * _diag[j]);
			}
		}
		return res;
	}
	
	public Matrix plus(Matrix m) {
		if (m.getColumnDimension() != _diag.length ||
			m.getRowDimension() != _diag.length) {
			throw new IllegalArgumentException("Inconsistent matrix dimensions");
		}
		Matrix res = (Matrix)m.clone();
		for (int k=0; k<_diag.length; k++) {
			res.set(k, k, res.get(k, k) + _diag[k]);
		}
		return res;
	}
}
