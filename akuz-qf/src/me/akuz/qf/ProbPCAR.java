package me.akuz.qf;

import me.akuz.core.math.DiagMatrix;
import me.akuz.core.math.MatrixUtils;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * Probabilistic PCA (closed form solution) with re-normalization.
 *
 */
public final class ProbPCAR {
	
	private final Matrix _X;
	private final int _startRow;
	private final int _endRow;
	private final int _factorCount;
	private final Matrix _myu;
	private final Matrix _S;
	private final Matrix _norm;
	private final Matrix _normS;
	private final Matrix _normW;
	private final Matrix _W;
	private final DiagMatrix _Ksi;
	private final Matrix _C;
	private final Matrix _GTranWInvKsi;
	
	public ProbPCAR(
			final Matrix mX, 
			final int factorCount) {
		
		this(
			mX,
			0,
			mX.getRowDimension(),
			factorCount);
	}
	
	public ProbPCAR(
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
		_norm = new Matrix(dataDim, 1);
		for (int i=0; i<dataDim; i++) {
			_norm.set(i, 0, Math.sqrt(_S.get(i, i)));
		}
		_normS = new Matrix(dataDim, dataDim);
		for (int i=0; i<dataDim; i++) {
			
			final double norm_i = _norm.get(i, 0);
			_normS.set(i, i, _S.get(i, i) / norm_i / norm_i);
			
			for (int j=i+1; j<dataDim; j++) {
				
				final double norm_j = _norm.get(j, 0);
				_normS.set(i, j, _S.get(i, j) / norm_i / norm_j);
				_normS.set(j, i, _S.get(j, i) / norm_i / norm_j);
			}
		}
		
		final SingularValueDecomposition normSvd = new SingularValueDecomposition(_normS);
		final Matrix normSvdS = normSvd.getS();
		final Matrix normU = MatrixUtils.getColumns(normSvd.getU(), 0, factorCount);
		final Matrix normL = new Matrix(factorCount, factorCount);
		for (int k=0; k<factorCount; k++) {
			normL.set(k, k, normSvdS.get(k, k));
		}
		
		double normSigma = 0;
		for (int k=factorCount; k<dataDim; k++) {
			normSigma += normSvdS.get(k, k);
		}
		normSigma /= (dataDim - factorCount);
		double normSigmaSq = normSigma*normSigma;
		
		final Matrix rightNormW = new Matrix(factorCount, factorCount);
		for (int k=0; k<factorCount; k++) {
			rightNormW.set(k, k, Math.sqrt(normL.get(k, k) - normSigmaSq));
		}
		_normW = normU.times(rightNormW);
		
		_W = new Matrix(dataDim, factorCount);
		for (int i=0; i<dataDim; i++) {
			for (int j=0; j<factorCount; j++) {
				_W.set(i, j, _normW.get(i, j) * _norm.get(i, 0));
			}
		}
		
		_Ksi = new DiagMatrix(dataDim);
		for (int i=0; i<dataDim; i++) {
			_Ksi.setDiag(i, normSigmaSq * _norm.get(i, 0));
		}

		_C = _Ksi.plus(_W.times(_W.transpose()));
		
		Matrix invG = _Ksi.inverse().timesOnLeft(_W.transpose()).times(_W);
		for (int i=0; i<invG.getRowDimension(); i++) {
			invG.set(i, i, invG.get(i, i) + 1);
		}
		Matrix G = invG.inverse();
		
		_GTranWInvKsi = G.times(_Ksi.inverse().timesOnLeft(_W.transpose()));
	}

	public Matrix getMyu() {
		return _myu;
	}
	
	public Matrix getS() {
		return _S;
	}
	
	public Matrix getW() {
		return _W;
	}
	
	public Matrix getC() {
		return _C;
	}
	
	public Matrix calcF() {
		final Matrix F = new Matrix(_X.getRowDimension(), _factorCount);
		for (int n=_startRow; n<_endRow; n++) {
			
			final Matrix x_n = MatrixUtils.getRows(_X, n, n+1).transpose();
			MatrixUtils.subtractEachColumn_inPlace(x_n, _myu);
			
			final Matrix f_n = _GTranWInvKsi.times(x_n);
			for (int f=0; f<_factorCount; f++) {
				F.set(n, f, f_n.get(f, 0));
			}
		}
		return F;
	}
}
