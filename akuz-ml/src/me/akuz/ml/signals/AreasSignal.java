package me.akuz.ml.signals;

import java.security.InvalidParameterException;

import me.akuz.core.math.MultidimPdf;
import me.akuz.core.math.StatsUtils;

public final class AreasSignal {
	
	private final MultidimPdf[] _areas;
	private final double[] _signals;
	private final double[] _areasProbs;
	
	public AreasSignal(MultidimPdf[] areas, double[] signals) {
		
		if (areas == null || areas.length == 0) {
			throw new InvalidParameterException("Areas parameter cannot be null or empty");
		}
		if (signals == null || signals.length == 0) {
			throw new InvalidParameterException("Signals parameter cannot be null or empty");
		}
		if (areas.length != signals.length) {
			throw new InvalidParameterException("Areas and signals arrays must be of the same length");
		}
		
		_areas = areas;
		_signals = signals;
		_areasProbs = new double[areas.length];
	}
	
	public double getSignal(double[] values) {
		
		// calc log probs
		double maxLogProb = 0;
		for (int i=0; i<_areas.length; i++) {
			double logProb = _areas[i].getLogProb(values);
			_areasProbs[i] = logProb;
			if (i == 0 || maxLogProb < logProb) {
				maxLogProb = logProb;
			}
		}
		
		// exponent into probs
		for (int i=0; i<_areas.length; i++) {
			_areasProbs[i] = Math.exp(_areasProbs[i] - maxLogProb); // maxLogProb is <= 0
		}
		
		// normalize probs
		StatsUtils.normalize(_areasProbs);

		// average signals
		double signal = 0;
		for (int i=0; i<_areas.length; i++) {
			signal += _signals[i] * _areasProbs[i];
		}
		
		return signal;
	}
	
	

}
