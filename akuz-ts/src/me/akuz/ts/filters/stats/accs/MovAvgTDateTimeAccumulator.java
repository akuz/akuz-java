package me.akuz.ts.filters.stats.accs;

import me.akuz.core.CircularBuffer;
import me.akuz.core.TDateTime;
import me.akuz.core.TDuration;
import me.akuz.ts.CurrTime;

public final class MovAvgTDateTimeAccumulator extends Accumulator<TDateTime> {

	private CircularBuffer<Double> _buff;
	private final TDuration _gapOkDur;
	private TDateTime _lastTime;
	private double _curr;
	
	public MovAvgTDateTimeAccumulator(
			final int sampleCount,
			final TDuration gapOkDur) {
		
		if (sampleCount < 2) {
			throw new IllegalArgumentException("Sample count must be >= 2");
		}
		if (gapOkDur.getMs() <= 0) {
			throw new IllegalArgumentException("GapOK duration must be positive");
		}
		_buff = new CircularBuffer<>(sampleCount);
		_gapOkDur = gapOkDur;
		_curr = 0.0;
	}
	
	@Override
	public void reset() {
		_buff.clear();
		_curr = 0.0;
	}

	@Override
	public void add(final TDateTime time, final Object value) {
		
		if (value != null) {
			if (!(value instanceof Number)) {
				throw new IllegalArgumentException(
						"Cannot compute moving average of non-numeric " + 
						"value (" + value.getClass().getSimpleName() + ")");
			}
		}
		
		CurrTime.checkNew(_lastTime, time);
		
		// reset if needed
		if (_lastTime != null) {
			final TDuration dur = new TDuration(_lastTime, time);
			if (dur.compareTo(_gapOkDur) > 0) {
				reset();
			}
		}
		
		// update current
		if (value != null) {

			final Double newValue = ((Number)value).doubleValue();
			final int oldSize = _buff.getCurrSize();
			final Double oldValue = _buff.add(newValue);
			final int newSize = _buff.getCurrSize();
			
			if (oldValue != null) {
				if (oldSize != newSize) {
					throw new IllegalStateException(
							"Curcular buffer error, it returned an old " + 
							"value and its size have changed after adding");
				}
				_curr -= oldValue.doubleValue() / newSize;
				_curr += newValue.doubleValue() / newSize;
			} else if (newSize == 1) {
				_curr = newValue.doubleValue();
			} else {
				_curr 
					= _curr / newSize * (newSize - 1)
					+ newValue.doubleValue() / newSize;
			}
			
			_lastTime = time;
		}
	}

	@Override
	public Object get() {
		return _buff.isFull() ? _curr : Double.NaN;
	}
	
	@Override
	public Accumulator<TDateTime> clone() {
		MovAvgTDateTimeAccumulator copy = (MovAvgTDateTimeAccumulator)super.clone();
		copy._buff = _buff.clone();
		return copy;
	}
}
