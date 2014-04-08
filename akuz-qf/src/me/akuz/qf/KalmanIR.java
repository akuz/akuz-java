package me.akuz.qf;

/**
 * Kalman filter for idiosyncratic returns.
 *
 */
public final class KalmanIR {
	
	private final double _decay;
	private double _par_zVar;
	private double _par_xVar;
	private boolean _isInitd;
	private double _curr_zMyu;
	private double _curr_zVar;
	
	public KalmanIR(final double decay, final double zVar, double xVar) {
		if (decay < 0 || decay > 1) {
			throw new IllegalArgumentException("Argument decay must be within interval [0, 1]");
		}
		_decay = decay;
		updateParameterVariances(zVar, xVar);
		_curr_zMyu = 0;
		_curr_zVar = zVar;
	}
	
	public void updateParameterVariances(final double zVar, final double xVar) {
		if (zVar <= 0) {
			throw new IllegalArgumentException("Argument zVar must be positive");
		}
		if (xVar <= 0) {
			throw new IllegalArgumentException("Argument xVar must be positive");
		}
		_par_zVar = zVar;
		_par_xVar = xVar;
	}
	
	public void addObservation(final double x) {
		
		if (_isInitd) { // update
			
			// helper 'matrix'
			final double P = _decay*_decay*_curr_zVar + _par_zVar;
			
			// kalman gain
			final double K = P / (P + _par_xVar);
			
			// update z myu
			_curr_zMyu = _decay * _curr_zMyu + K * (x - _decay * _curr_zMyu);
			
			// update z var
			_curr_zVar = (1.0 - K) * P;
			
			
		} else { // initialize
			
			// kalman gain
			final double K = _par_zVar / (_par_zVar + _par_xVar);

			// update z myu
			_curr_zMyu = K * x;
			
			// update z var
			_curr_zVar = (1.0 - K) * _par_zVar;

			// initialized
			_isInitd = true;
		}
	}
	
	public double getZMyu() {
		return _curr_zMyu;
	}
	
	public double getZVar() {
		return _curr_zVar;
	}

}
