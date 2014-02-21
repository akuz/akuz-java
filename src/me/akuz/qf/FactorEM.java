package me.akuz.qf;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.DiagMatrix;
import me.akuz.core.math.MatrixUtils;
import me.akuz.core.math.StatsUtils;
import Jama.Matrix;

/**
 * Expectation Maximization (EM) inference for the factor model with non-zero factor bias.
 *
 */
public final class FactorEM {
	
	private static final double INIT_W_RANGE = 0.3;
	private static final double INIT_COV_BASE = 0.1;
	private static final double INIT_COV_RAND = 0.05;

	private final Matrix _mX;
	private final int _startRow;
	private final int _endRow;
	private final Matrix _mS;
	private final int _factorCount;
	private final int _variableCount;
	private final int _sampleCount;
	private Matrix[] _xExpectedFactor;
	private Matrix[] _xExpectedFactorFactor;
	private Matrix _pFactorBias;
	private DiagMatrix _pFactorPhi;
	private Matrix _pW;
	private Matrix _pVariableBias;
	private DiagMatrix _pVariableKsi;
	
	public FactorEM(
			final Matrix mX, 
			final int startRow, 
			final int endRow, 
			final int factorCount) {
		
		if (mX == null || mX.getRowDimension() == 0 || mX.getColumnDimension() == 0) {
			throw new IllegalArgumentException("Matrix X must not be null or empty");
		}
		if (factorCount < 1) {
			throw new IllegalArgumentException("Factor count must be positive");
		}
		_mX = mX;
		_startRow = startRow;
		_endRow = endRow;
		_mS = StatsUtils.calcSampleCovarianceMatrix(mX, startRow, endRow);
		_factorCount = factorCount;
		_variableCount = mX.getColumnDimension();
		_sampleCount = endRow - startRow;
		
		// init parameters
		final Random rnd = ThreadLocalRandom.current();
		_pFactorBias = new Matrix(_factorCount, 1);
		_pFactorPhi = new DiagMatrix(_factorCount);
		for (int f=0; f<_factorCount; f++) {
			_pFactorPhi.setDiag(f, INIT_COV_BASE + INIT_COV_RAND * rnd.nextDouble());
		}
		_pW = new Matrix(_variableCount, _factorCount);
		for (int v=0; v<_variableCount; v++) {
			for (int f=0; f<_factorCount; f++) {
				_pW.set(v, f, INIT_W_RANGE * (1.0 - 2.0 * rnd.nextDouble()));
			}
		}
		_pVariableBias = new Matrix(_variableCount, 1);
		_pVariableKsi = new DiagMatrix(_variableCount);
		for (int v=0; v<_variableCount; v++) {
			_pVariableKsi.setDiag(v, INIT_COV_BASE + INIT_COV_RAND * rnd.nextDouble());
		}
	}
	
	public void execute(final int iterationCount) {
		
		if (_xExpectedFactor == null) {
			_xExpectedFactor = new Matrix[_mX.getRowDimension()];
			_xExpectedFactorFactor = new Matrix[_mX.getRowDimension()];
		}
		
		for (int iter=1; iter<=iterationCount; iter++) {
			
			// ***********
			// expectation
			Matrix G = _pFactorPhi.inverse().plus(_pVariableKsi.inverse().timesOnLeft(_pW.transpose()).times(_pW)).inverse();
			Matrix WTranKsiInv = _pVariableKsi.inverse().timesOnLeft(_pW.transpose());
			for (int n=_startRow; n<_endRow; n++) {

				Matrix x_n = MatrixUtils.getRows(_mX, n, n+1).transpose();
				_xExpectedFactor[n] = G.times(WTranKsiInv.times(x_n.minus(_pVariableBias)).plus(_pFactorPhi.inverse().timesOnRight(_pFactorBias)));
				_xExpectedFactorFactor[n] = G.plus(_xExpectedFactor[n].times(_xExpectedFactor[n].transpose()));
			}
			
			// ************
			// maximization
			_pFactorBias = new Matrix(_factorCount, 1);
			Matrix factorSampleCovariance = new Matrix(_factorCount, _factorCount);
			_pVariableBias = new Matrix(_variableCount, 1);
			for (int n=_startRow; n<_endRow; n++) {
				
				Matrix x_n = MatrixUtils.getRows(_mX, n, n+1).transpose();
				_pFactorBias.plusEquals(_xExpectedFactor[n].times(1.0/_sampleCount));
				factorSampleCovariance.plusEquals(_xExpectedFactorFactor[n].times(1.0/_sampleCount));
				_pVariableBias.plusEquals(x_n.minus(_pW.times(_xExpectedFactor[n])).times(1.0/_sampleCount));
			}
			_pFactorPhi = new DiagMatrix(factorSampleCovariance.minus(_pFactorBias.times(_pFactorBias.transpose())));
			Matrix leftW = new Matrix(_variableCount, _factorCount);
			Matrix rightW = new Matrix(_factorCount, _factorCount);
			for (int n=_startRow; n<_endRow; n++) {
				
				Matrix x_n = MatrixUtils.getRows(_mX, n, n+1).transpose();
				leftW.plusEquals(x_n.minus(_pVariableBias).times(_xExpectedFactor[n].transpose()));
				rightW.plusEquals(_xExpectedFactorFactor[n]);
			}
			_pW = leftW.times(rightW.inverse());
			Matrix rightKsi = leftW.transpose().timesEquals(1.0/_sampleCount);
			_pVariableKsi = new DiagMatrix(_mS.minus(_pW.times(rightKsi)));
		}
	}
	
	public Matrix collectFactorHistory() {
		
		Matrix res = new Matrix(_xExpectedFactor.length, _factorCount);
		for (int n=_startRow; n<_endRow; n++) {
			for (int k=0; k<_factorCount; k++) {
				res.set(n, k, _xExpectedFactor[n].get(k, 0));
			}
		}
		return res;
	}
	
	public Matrix getFactorBias() {
		return _pFactorBias;
	}
	
	public DiagMatrix getFactorPhi() {
		return _pFactorPhi;
	}
	
	public Matrix getW() {
		return _pW;
	}
	
	public Matrix getVariableBias() {
		return _pVariableBias;
	}
	
	public DiagMatrix getVariableKsi() {
		return _pVariableKsi;
	}
	
}
