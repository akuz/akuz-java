package me.akuz.qf;

import Jama.Matrix;

public final class KalmanMP {
	
	private final double _halfLife;
	private final double _kappa;
	private final double _initPrice;
	private final Matrix _A;
	private final Matrix _G;
	private final Matrix _C;
	private final Matrix _S;
	private final Matrix _I;
	
	private Matrix _currZMyu;
	private Matrix _currZVar;
	
	public KalmanMP(
			final double halfLife, 
			final double initPrice,
			final double observationVar,
			final double spreadVar) {
		
		_halfLife = halfLife;
		_kappa = Math.log(2)/_halfLife;
		_initPrice = initPrice;
		
		_A = new Matrix(2, 2);
		_A.set(0, 0, 1.0);
		_A.set(1, 1, 1.0 - _kappa);
		
		_G = new Matrix(2, 2);
		updateObservationVar(observationVar);
		
		_C = new Matrix(1, 2, 1.0);
		
		_S = new Matrix(1, 1, spreadVar);
		
		_I = new Matrix(2, 2);
		_I.set(0, 0, 1.0);
		_I.set(1, 1, 1.0);
	}
	
	public void updateObservationVar(final double observationVar) {
		_G.set(0, 0, observationVar / 2.0);
		_G.set(1, 1, observationVar / 2.0 * _kappa);
	}
	
	public void addObservation(final double value) {
		
		final Matrix x = new Matrix(1, 1, value);
		
		if (_currZMyu == null) {
			
			final Matrix myu0 = new Matrix(2, 1);
			myu0.set(0, 0, _initPrice);
			myu0.set(1, 0, 0.0);
			
			final Matrix P0 = _G;
			
			final Matrix K1 = P0.times(_C.transpose()).times(_C.times(P0).times(_C.transpose()).plusEquals(_S).inverse());
			
			_currZMyu = myu0.plus(K1.times(x.minus(_C.times(myu0))));
			
			_currZVar = _I.minus(K1.times(_C)).times(P0);
			
		} else {
			
			final Matrix Pn_1 = _A.times(_currZVar).times(_A.transpose()).plus(_G);
			
			final Matrix Kn = Pn_1.times(_C.transpose()).times(_C.times(Pn_1).times(_C.transpose()).plusEquals(_S).inverse());
			
			_currZMyu = _A.times(_currZMyu).plus(Kn.times(x.minus(_C.times(_A).times(_currZMyu))));
			
			_currZVar = _I.minus(Kn.times(_C)).times(Pn_1);
		}
	}
	
	public double getCurrentPrice() {
		return _currZMyu.get(0, 0);
	}
	
	public double getCurrentMispricing() {
		return _currZMyu.get(1, 0);
	}

}
