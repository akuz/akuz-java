package me.akuz.core.math;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Jama.Matrix;

public final class SpaMatrix implements Cloneable {
	
	private int _m;
	private int _n;
	private List<Object> _rows;
	private List<Object> _cols;
	private SpaMatrixLine[] _rowLines;
	private SpaMatrixLine[] _colLines;
	private boolean _isOptimized;
	
	public static final int OPT_MULT_BOTH_WAYS = 0;
	public static final int OPT_MULT_ON_RIGHT_BY = 1;
	public static final int OPT_MULT_ON_LEFT_BY = 2;
	
	private static final int ACT_TRANSPOSE_VIEW = 0;
	
	private SpaMatrix(SpaMatrix o, int act) {
		if (act == ACT_TRANSPOSE_VIEW) {
			if (!o._isOptimized) {
				throw new IllegalStateException("Sparse matrix is not optimized, cannot transpose.");
			}
			_m = o._n;
			_n = o._m;
			_rowLines = o._colLines;
			_colLines = o._rowLines;
			_isOptimized = true;
		} else {
			throw new InvalidParameterException("Parameter act is invalid.");
		}
	}
	
	public SpaMatrix(SpaMatrix copyFrom) {
		if (copyFrom == null) {
			throw new NullPointerException("copyFrom");
		}
		if (!copyFrom._isOptimized) {
			throw new InvalidParameterException("Source sparse matrix must be optimized before being copied from.");
		}
		_m = copyFrom._m;
		_n = copyFrom._n;
		_rowLines = copyFrom._rowLines.clone();
		if (_rowLines != null) {
			for (int i=0; i<_rowLines.length; i++) {
				if (_rowLines[i] != null) {
					_rowLines[i] = new SpaMatrixLine(_rowLines[i]);
				}
			}
		}
		_colLines = copyFrom._colLines.clone();
		if (_colLines != null) {
			for (int j=0; j<_colLines.length; j++) {
				if (_colLines[j] != null) {
					_colLines[j] = new SpaMatrixLine(_colLines[j]);
				}
			}
		}
		_isOptimized = true;
	}
	
	public SpaMatrix(int m, int n) {
		this(m, n, OPT_MULT_BOTH_WAYS);
	}
	
	public SpaMatrix(int m, int n, int opt) {
		_m = m;
		_n = n;
		if (opt == OPT_MULT_BOTH_WAYS || opt == OPT_MULT_ON_RIGHT_BY) {
			_rows = new ArrayList<Object>();
			for (int i=0; i<m; i++) {
				_rows.add(null);
			}
		}
		if (opt == OPT_MULT_BOTH_WAYS || opt == OPT_MULT_ON_LEFT_BY) {
			_cols = new ArrayList<Object>();
			for (int j=0; j<n; j++) {
				_cols.add(null);
			}
		}
		if (_rows == null && _cols == null) {
			throw new InvalidParameterException("Parameter opt is invalid.");
		}
		_rowLines = null;
		_colLines = null;
		_isOptimized = false;
	}
	
	public SpaMatrix(Matrix m) {
		this(m, OPT_MULT_BOTH_WAYS);
	}
	
	public SpaMatrix(Matrix m, int opt) {
		this(m.getRowDimension(), m.getColumnDimension(), opt);
		for (int i=0; i<_m; i++) {
			for (int j=0; j<_n; j++) {
				double value = m.get(i, j);
				if (value != 0.0) {
					set(i, j, value);
				}
			}
		}
	}
	
	public int getRowCount() {
		return _m;
	}
	
	public int getColCount() {
		return _n;
	}
	
	public void set(int i, int j, double value) {
		if (_isOptimized) {
			if (_rowLines != null) {
				SpaMatrixLine rowLine = _rowLines[i];
				if (rowLine == null) {
					throw new InvalidParameterException("Optmized cell (" + i + ", " + j + ") does not exist");
				}
				if (!rowLine.set(j, value)) {
					throw new InvalidParameterException("Optmized cell (" + i + ", " + j + ") does not exist");
				}
			}
			if (_colLines != null) {
				SpaMatrixLine colLine = _colLines[j];
				if (colLine == null) {
					throw new InvalidParameterException("Optmized cell (" + i + ", " + j + ") does not exist");
				}
				if (!colLine.set(i, value)) {
					throw new InvalidParameterException("Optmized cell (" + i + ", " + j + ") does not exist");
				}
			}
		} else {
			if (_rows != null) {
				@SuppressWarnings("unchecked")
				Map<Integer, Double> row = (Map<Integer, Double>)_rows.get(i);
				if (row == null) {
					row = new HashMap<Integer, Double>();
					_rows.set(i, row);
				}
				row.put(j, value);
			}
			if (_cols != null) {
				@SuppressWarnings("unchecked")
				Map<Integer, Double> col = (Map<Integer, Double>)_cols.get(j);
				if (col == null) {
					col = new HashMap<Integer, Double>();
					_cols.set(j, col);
				}
				col.put(i, value);
			}
		}
	}
	
	public double get(int i, int j) {
		if (_isOptimized) {
			if (_rowLines != null) {
				SpaMatrixLine rowLine = _rowLines[i];
				return rowLine == null ? 0.0 : rowLine.get(j);
			}
			if (_colLines != null) {
				SpaMatrixLine colLine = _colLines[j];
				return colLine == null ? 0.0 : colLine.get(i);
			}
			throw new InvalidParameterException("Matrix cell (" + i + ", " + j + ") does not exist");
		} else {
			if (_rows != null) {
				@SuppressWarnings("unchecked")
				Map<Integer, Double> row = (Map<Integer, Double>)_rows.get(i);
				Double value = (row != null) ? row.get(j) : null;
				return value != null ? value : 0.0;
			}
			if (_cols != null) {
				@SuppressWarnings("unchecked")
				Map<Integer, Double> col = (Map<Integer, Double>)_cols.get(j);
				Double value = (col != null) ? col.get(i) : null;
				return value == null ? 0.0 : value;
			}
			throw new InvalidParameterException("Matrix cell (" + i + ", " + j + ") does not exist");
		}
	}
	
	public SpaMatrixLine getRow(int i) {
		if (!_isOptimized) {
			throw new IllegalStateException("Sparse matrix is not optimized yet, cannot get rows.");
		}
		if (_rowLines == null) {
			throw new IllegalStateException("Sparse matrix is not optimized for row access.");
		}
		return _rowLines[i];
	}
	
	public SpaMatrixLine getColumn(int j) {
		if (!_isOptimized) {
			throw new IllegalStateException("Sparse matrix is not optimized yet, cannot get columns.");
		}
		if (_colLines == null) {
			throw new IllegalStateException("Sparse matrix is not optimized for column access.");
		}
		return _colLines[j];
	}
	
	public int addRow() {
		if (_isOptimized) {
			throw new IllegalStateException("Sparse matrix is already optimized, cannot add rows");
		}
		if (_rows != null) {
			_rows.add(null);
		}
		_m++;
		return _m-1;
	}
	
	public int addColumn() {
		if (_isOptimized) {
			throw new IllegalStateException("Sparse matrix is already optimized, cannot add rows");
		}
		if (_cols != null) {
			_cols.add(null);
		}
		_n++;
		return _n-1;
	}
	
	public final Matrix multOnRightBy(Matrix right) {
		if (_n != right.getRowDimension()) {
			throw new InvalidParameterException("Matrix dimensions do not match");
		}
		if (_rowLines == null) {
			throw new IllegalStateException("This sparse matrix is not optimised for multiplication on the right");
		}
		
		Matrix res = new Matrix(_m, right.getColumnDimension());
		for (int i=0; i<res.getRowDimension(); i++) {
			for (int j=0; j<res.getColumnDimension(); j++) {
				SpaMatrixLine rowLine = _rowLines[i];
				if (rowLine != null) {
					double sum = 0;
					for (int idx=0; idx<rowLine.size(); idx++) {
						int k = rowLine.getIndexByIdx(idx);
						double leftValue = rowLine.getValueByIdx(idx);
						double rightValue = right.get(k, j);
						sum += leftValue*rightValue;
					}
					res.set(i, j, sum);
				}
			}
		}
		
		return res;
	}
	
	public final Matrix multOnLeftBy(Matrix left) {
		if (_m != left.getColumnDimension()) {
			throw new InvalidParameterException("Matrix dimensions do not match");
		}
		if (_colLines == null) {
			throw new IllegalStateException("This sparse matrix is not optimised for multiplication on the left");
		}
		
		Matrix res = new Matrix(left.getRowDimension(), _n);
		for (int i=0; i<res.getRowDimension(); i++) {
			for (int j=0; j<res.getColumnDimension(); j++) {
				SpaMatrixLine col = _colLines[j];
				if (col != null) {
					double sum = 0;
					for (int idx=0; idx<col.size(); idx++) {
						int k = col.getIndexByIdx(idx);
						double rightValue = col.getValueByIdx(idx);
						double leftValue = left.get(i, k);
						sum += rightValue*leftValue;
					}
					res.set(i, j, sum);
				}
			}
		}
		
		return res;
	}
	
	public Matrix toDense() {
		Matrix m = new Matrix(_m, _n);
		if (_rowLines != null) {
			for (int i=0; i<_m; i++) {
				SpaMatrixLine row = _rowLines[i];
				if (row != null) {
					for (int idx=0; idx<row.size(); idx++) {
						Integer j = row.getIndexByIdx(idx);
						m.set(i, j, row.getValueByIdx(idx));
					}
				}
			}
		} else if (_colLines != null) {
			for (int j=0; j<_n; j++) {
				SpaMatrixLine col = _colLines[j];
				if (col != null) {
					for (int idx=0; idx<col.size(); idx++) {
						Integer i = col.getIndexByIdx(idx);
						m.set(i, j, col.getValueByIdx(idx));
					}
				}
			}
		} else {
			throw new IllegalStateException("Matrix has not been optimized yet, call optimize() first.");
		}
		return m;
	}
	
	public static SpaMatrix randomGaussian(int m, int n, double fillPercentage) {
		return randomGaussian(m, n, fillPercentage, OPT_MULT_BOTH_WAYS);
	}
	
	public static SpaMatrix randomGaussian(int m, int n, double fillPercentage, int opt) {
		Random random = new Random(System.currentTimeMillis());
		SpaMatrix result = new SpaMatrix(m, n, opt);
		double entries = m*n*fillPercentage;
		for (int k=0; k<entries; k++) {
			int i = (int)Math.floor(random.nextDouble()*m);
			int j = (int)Math.floor(random.nextDouble()*n);
			result.set(i, j, random.nextGaussian());
		}
		result.optimize();
		return result;
	}
	
	public void optimize() {
		if (_isOptimized) {
			throw new IllegalStateException("Sparse matrix is already optimized");
		}
		if (_rows != null) {
			_rowLines = new SpaMatrixLine[_rows.size()];
			for (int i=0; i<_m; i++) {
				@SuppressWarnings("unchecked")
				Map<Integer, Double> row = (Map<Integer, Double>)_rows.get(i);
				if (row != null) {
					SpaMatrixLine line = new SpaMatrixLine(row.size());
					Iterator<Integer> iter_j = row.keySet().iterator();
					int idx = 0;
					while (iter_j.hasNext()) {
						Integer j = iter_j.next();
						line.set(idx, j, row.get(j));
						idx++;
					}
					_rowLines[i] = line;
					_rows.set(i, null);
				}
			}
			_rows = null;
		}
		if (_cols != null) {
			_colLines = new SpaMatrixLine[_cols.size()];
			for (int j=0; j<_n; j++) {
				@SuppressWarnings("unchecked")
				Map<Integer, Double> col = (Map<Integer, Double>)_cols.get(j);
				if (col != null) {
					SpaMatrixLine line = new SpaMatrixLine(col.size());
					Iterator<Integer> iter_i = col.keySet().iterator();
					int idx = 0;
					while (iter_i.hasNext()) {
						Integer i = iter_i.next();
						line.set(idx, i, col.get(i));
						idx++;
					}
					_colLines[j] = line;
					_cols.set(j, null);
				}
			}
			_cols = null;
		}
		_isOptimized = true;
	}
	
	public SpaMatrix transposedView() {
		return new SpaMatrix(this, ACT_TRANSPOSE_VIEW);
	}
}
