package me.akuz.qf;

public final class Kalman1 {
	
	public final double _zMyu0;
	public final double _zVar0;
	
	private double _parZVar;
	private double _parXVar;
	private boolean _initd;
	private double _currZMyu;
	private double _currZVar;
	
	public Kalman1(final double zMyu0, final double zVar0, final double zVar, final double xVar) {
		if (zVar0 < 0) {
			throw new IllegalArgumentException("Argument var0 must be >= 0");
		}
		_zMyu0 = zMyu0;
		_zVar0 = zVar0;
		updateParameterVariances(zVar, xVar);
		_currZMyu = zMyu0;
		_currZVar = zVar0;
	}
	
	public void updateParameterVariances(final double zVar, final double xVar) {
		if (zVar <= 0) {
			throw new IllegalArgumentException("Argument zVar must be positive");
		}
		if (xVar <= 0) {
			throw new IllegalArgumentException("Argument xVar must be positive");
		}
		_parZVar = zVar;
		_parXVar = xVar;
	}
	
	public void addObservation(final double x) {
		
		if (_initd) {
			
			final double P = _currZVar + _parZVar;
			
			final double K = P / (P + _parXVar);
			
			_currZMyu = _currZMyu + K * (x - _currZMyu);
			
			_currZVar = (1.0 - K) * P;
			
		} else {
			
			final double K1 = _zVar0 / (_zVar0 + _parXVar);
			
			_currZMyu = _zMyu0 + K1 * (x - _zMyu0);
			
			_currZVar = (1.0 - K1) * _zVar0;
			
			_initd = true;
		}
	}
	
	public double getCurrZMyu() {
		return _currZMyu;
	}
	
	public double getCurrZVar() {
		return _currZVar;
	}

}
