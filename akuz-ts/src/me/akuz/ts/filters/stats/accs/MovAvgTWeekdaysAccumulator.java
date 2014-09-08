package me.akuz.ts.filters.stats.accs;

import me.akuz.core.CircularBuffer;
import me.akuz.core.TDate;
import me.akuz.core.TWeekdays;
import me.akuz.ts.CurrTime;

public final class MovAvgTWeekdaysAccumulator extends Accumulator<TDate> {

	private final CircularBuffer<Double> _buff;
	private final int _gapOkWeekdays;
	private TDate _lastTime;
	private double _curr;
	
	public MovAvgTWeekdaysAccumulator(
			final int sampleCount,
			final int gapOkWeekdays) {
		
		if (sampleCount < 2) {
			throw new IllegalArgumentException("Sample count must be >= 2");
		}
		if (gapOkWeekdays <= 0) {
			throw new IllegalArgumentException("GapOK weekdays must be positive");
		}
		_buff = new CircularBuffer<>(sampleCount);
		_gapOkWeekdays = gapOkWeekdays;
		_curr = 0.0;
	}
	
	@Override
	public void reset() {
		_buff.clear();
		_curr = 0.0;
	}

	@Override
	public void add(final TDate time, final Object value) {
		
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
			final int distance = TWeekdays.distance(_lastTime, time);
			if (distance > _gapOkWeekdays) {
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
}
