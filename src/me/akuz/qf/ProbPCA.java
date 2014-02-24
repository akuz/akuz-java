package me.akuz.qf;

import me.akuz.core.math.MatrixUtils;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * Probabilistic PCA (closed form solution).
 *
 */
public final class ProbPCA {
	
	private final Matrix _X;
	private final int _startRow;
	private final int _endRow;
	private final int _factorCount;
	private final Matrix _myu;
	private final Matrix _S;
	private final double _sigma;
	private final double _sigmaSq;
	private final Matrix _W;
	private final Matrix _C;
	private final Matrix _M;
	private final Matrix _invM;
	private final Matrix _invM_tranW;
	
	public ProbPCA(
			final Matrix mX, 
			final int factorCount) {
		
		this(
			mX,
			0,
			mX.getRowDimension(),
			factorCount);
	}
	
	public ProbPCA(
			final Matrix X, 
			final int startRow,
			final int endRow,
			final int factorCount) {
		
		if (X == null || X.getRowDimension() == 0 || X.getColumnDimension() == 0) {
			throw new IllegalArgumentException("Matrix X must not be null or empty");
		}
		if (factorCount < 1) {
			throw new IllegalArgumentException("Factor count (" + factorCount + ") must be positive");
		}
		final int dataDim = X.getColumnDimension();
		final int dataCount = endRow - startRow;
		if (factorCount >= dataDim) {
			throw new IllegalArgumentException("Factor count (" + factorCount + ") must be less than data dimension (column count " + dataDim + ")");
		}
		if (factorCount >= dataCount) {
			throw new IllegalArgumentException("Factor count (" + factorCount + ") must be less than data count (row count " + dataCount + ")");
		}
		
		_X = X;
		_startRow = startRow;
		_endRow = endRow;
		_factorCount = factorCount;
		_myu = MatrixUtils.averageRows(X, startRow, endRow).transpose();
		
		_S = new Matrix(dataDim, dataDim);
		for (int dataIndex=startRow; dataIndex<endRow; dataIndex++) {
			
			for (int i=0; i<dataDim; i++) {
				
				final double diff_i = X.get(dataIndex, i) - _myu.get(i, 0);
				_S.set(i, i, _S.get(i, i) + diff_i*diff_i/dataCount);
				
				for (int j=i+1; j<dataDim; j++) {

					final double diff_j = X.get(dataIndex, j) - _myu.get(j, 0);
					final double add_ij = diff_i*diff_j/dataCount;
					_S.set(i, j, _S.get(i, j) + add_ij);
					_S.set(j, i, _S.get(j, i) + add_ij);
				}
			}
		}
		
		final SingularValueDecomposition svd = new SingularValueDecomposition(_S);
		final Matrix svdS = svd.getS();
		final Matrix U = MatrixUtils.getColumns(svd.getU(), 0, factorCount);
		final Matrix L = new Matrix(factorCount, factorCount);
		for (int k=0; k<factorCount; k++) {
			L.set(k, k, svdS.get(k, k));
		}
		
		double sigma = 0;
		for (int k=factorCount; k<dataDim; k++) {
			sigma += svdS.get(k, k);
		}
		sigma /= (dataDim - factorCount);
		_sigma = sigma;
		_sigmaSq = sigma*sigma;
		
		final Matrix rightW = new Matrix(factorCount, factorCount);
		for (int k=0; k<factorCount; k++) {
			rightW.set(k, k, Math.sqrt(L.get(k, k) - _sigmaSq));
		}
		_W = U.times(rightW);

		_C = _W.times(_W.transpose());
		for (int j=0; j<dataDim; j++) {
			_C.set(j, j, _C.get(j, j) + _sigmaSq);
		}
		
		_M = _W.transpose().times(_W);
		for (int k=0; k<factorCount; k++) {
			_M.set(k, k, _M.get(k, k) + _sigmaSq);
		}
		
		_invM = _M.inverse();
		_invM_tranW = _invM.times(_W.transpose());
	}

	public Matrix getX() {
		return _X;
	}

	public Matrix getMyu() {
		return _myu;
	}
	
	public Matrix getS() {
		return _S;
	}
	
	public double getSigma() {
		return _sigma;
	}
	
	public double getSigmaSq() {
		return _sigmaSq;
	}
	
	public Matrix getW() {
		return _W;
	}
	
	public Matrix getC() {
		return _C;
	}
	
	public Matrix getM() {
		return _M;
	}
	
	public Matrix getInvM() {
		return _invM;
	}
	
	public Matrix getInvMTRanW() {
		return _invM_tranW;
	}
	
	public Matrix calcF() {
		final Matrix F = new Matrix(_X.getRowDimension(), _factorCount);
		for (int n=_startRow; n<_endRow; n++) {
			
			final Matrix x_n = MatrixUtils.getRows(_X, n, n+1).transpose();
			MatrixUtils.subtractEachColumn_inPlace(x_n, _myu);
			
			final Matrix f_n = _invM_tranW.times(x_n);
			for (int f=0; f<_factorCount; f++) {
				F.set(n, f, f_n.get(f, 0));
			}
		}
		return F;
	}
}
