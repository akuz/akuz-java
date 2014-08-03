package me.akuz.core.math;

import java.security.InvalidParameterException;
import java.util.Random;

import Jama.Matrix;
import Jama.QRDecomposition;
import Jama.SingularValueDecomposition;

public final class RandomizedSVD {
	
	private final Random _random;
	private final SpaMatrix _A;
	private final int _k;
	private final int _q;
	
	private Matrix _Omega;
	private Matrix _Y;
	private Matrix _Q;
	private Matrix _U;
	private Matrix _S;
	private Matrix _V;
	
	public RandomizedSVD(final SpaMatrix A, final int k, final int q) {
		if (A == null) {
			throw new NullPointerException();
		}
		if (k < 1) {
			throw new InvalidParameterException("k must me positive");
		}
		if (q < 0) {
			throw new InvalidParameterException("p must me non-negative");
		}
		if (2*k > Math.min(A.getColCount(), A.getRowCount())) {
			throw new InvalidParameterException("2*k must not be larger than minimum matrix dimension size");
		}
		_random = new Random(System.currentTimeMillis());
		_A = A;
		_k = k;
		_q = q;
		execute();
	}

	private void execute() {
		
		long millis;
		System.out.println("Executing Randomized SVD...");
		
		System.out.print("Generating Omega (" + _A.getColCount() + ", " + 2*_k + ")... ");
		millis = System.currentTimeMillis();
		_Omega = new Matrix(_A.getColCount(), 2*_k);
		for (int i=0; i<_Omega.getRowDimension(); i++) {
			for (int j=0; j<_Omega.getColumnDimension(); j++) {
				_Omega.set(i, j, _random.nextGaussian());
			}
		}
		System.out.println(System.currentTimeMillis() - millis + "ms");
		
		System.out.print("Multiplying A by Omega... ");
		millis = System.currentTimeMillis();
		_Y = _A.multOnRightBy(_Omega);
		System.out.println(System.currentTimeMillis() - millis + "ms");
		System.out.println("Obtained Y (" + _Y.getRowDimension() + ", " + _Y.getColumnDimension() + ")");
		
		SpaMatrix AT = _A.transposedView();
		for (int iteration=1; iteration<=_q; iteration++) {
			System.out.print("Power iteration " + iteration + "... ");
			millis = System.currentTimeMillis();
			System.out.print("Mult by A^T... ");
			_Y = AT.multOnRightBy(_Y);
			System.out.print("Mult by A... ");
			_Y = _A.multOnRightBy(_Y);
			System.out.println(System.currentTimeMillis() - millis + "ms");
			System.out.println("Updated Y (" + _Y.getRowDimension() + ", " + _Y.getColumnDimension() + ")");
		}
		
		System.out.print("Performing QR-decomposition... ");
		millis = System.currentTimeMillis();
		QRDecomposition qr = new QRDecomposition(_Y);
		_Q = qr.getQ();
		System.out.println(System.currentTimeMillis() - millis + "ms");
		System.out.println("Obtained Q (" + _Q.getRowDimension() + ", " + _Q.getColumnDimension() + ")");
		
		System.out.print("Transposing Q... ");
		millis = System.currentTimeMillis();
		Matrix QT = _Q.transpose();
		System.out.println(System.currentTimeMillis() - millis + "ms");
		
		System.out.print("Calculating matrix B... ");
		millis = System.currentTimeMillis();
		Matrix B = _A.multOnLeftBy(QT);
		System.out.println(System.currentTimeMillis() - millis + "ms");
		System.out.println("Obtained B (" + B.getRowDimension() + ", " + B.getColumnDimension() + ")");
		
		System.out.print("Calculating matrix BxB^T... ");
		millis = System.currentTimeMillis();
		Matrix BT = B.transpose();
		Matrix BBT = B.times(BT);
		System.out.println(System.currentTimeMillis() - millis + "ms");
		System.out.println("Obtained B*B^T (" + BBT.getRowDimension() + ", " + BBT.getColumnDimension() + ")");
		
		System.out.print("Calculating full SVD of small BxB^T... ");
		millis = System.currentTimeMillis();
		SingularValueDecomposition svd = new SingularValueDecomposition(BBT);
		System.out.println(System.currentTimeMillis() - millis + "ms");
		
		Matrix Uhat = svd.getU();
		System.out.println("Obtained Uhat (" + Uhat.getRowDimension() + ", " + Uhat.getColumnDimension() + ")");
		
		System.out.print("Calculating U = Q * Uhat... ");
		millis = System.currentTimeMillis();
		_U = _Q.times(Uhat);
		System.out.println(System.currentTimeMillis() - millis + "ms");
		System.out.println("Obtained U (" + _U.getRowDimension() + ", " + _U.getColumnDimension() + ")");
		
		System.out.print("Calculating Sigma... ");
		millis = System.currentTimeMillis();
		_S = svd.getS().copy();
		for (int d=0; d<_S.getColumnDimension(); d++) {
			_S.set(d, d, Math.sqrt(_S.get(d, d)));
		}
		System.out.println(System.currentTimeMillis() - millis + "ms");
		System.out.println("Obtained Sigma (" + _S.getRowDimension() + ", " + _S.getColumnDimension() + ")");
		
		System.out.print("Calculating V... ");
		millis = System.currentTimeMillis();
		Matrix SigmaInv = _S.copy();
		for (int d=0; d<SigmaInv.getColumnDimension(); d++) {
			double value = SigmaInv.get(d, d);
			if (value != 0.0) {
				SigmaInv.set(d, d, 1.0/value);
			}
		}
		Matrix UT = _U.transpose();
		_V = SigmaInv.times(_A.multOnLeftBy(UT));
		_V = _V.transpose();
		System.out.println(System.currentTimeMillis() - millis + "ms");
		System.out.println("Obtained V (" + _V.getRowDimension() + ", " + _V.getColumnDimension() + ")");
	
		System.out.println("Randomized SVD done.");
	}

	public Matrix getU() {
		return _U;
	}

	public Matrix getS() {
		return _S;
	}

	public Matrix getV() {
		return _V;
	}
}
