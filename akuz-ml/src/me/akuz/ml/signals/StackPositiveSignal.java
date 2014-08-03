package me.akuz.ml.signals;

import java.security.InvalidParameterException;

public final class StackPositiveSignal {
	
	private final AreasSignal[] _signals;
	
	public StackPositiveSignal(AreasSignal[] signals) {
		if (signals == null || signals.length == 0) {
			throw new InvalidParameterException("Signals array must not be null or empty");
		}
		_signals = signals;
	}
	
	public double getSignal(double[][] values) {
		
		double signal = 1.0;
		
		for (int i=0; i<_signals.length; i++) {
			
			double iSignal = _signals[i].getSignal(values[i]);

			if (iSignal < 0) {
				signal = 0;
				break;
			} else {
				if (signal > iSignal) {
					signal = iSignal;
				}
			}
		}
		
		return signal;
	}

}
