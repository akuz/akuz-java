package me.akuz.ts.filters.stats.accs;

import me.akuz.core.CircularBuffer;
import me.akuz.core.TDateTime;
import me.akuz.core.TDuration;

public final class MovAvgTDateTimeAccumulator extends Accumulator<TDateTime> {

	private final int _sampleCount;
	private final TDuration _gapOkDur;
	private final CircularBuffer<Double> _buff;
	private double _currValue;
	
	public MovAvgTDateTimeAccumulator(
			final int sampleCount,
			final TDuration gapOkDur) {
		
		if (sampleCount < 2) {
			throw new IllegalArgumentException("Sample count must be >= 2");
		}
		if (gapOkDur.getMs() <= 0) {
			throw new IllegalArgumentException("GapOK duration must be positive");
		}
		_sampleCount = sampleCount;
		_gapOkDur = gapOkDur;
		_buff = new CircularBuffer<>(sampleCount);
		_currValue = Double.NaN;
	}
	
	@Override
	public void reset() {
		_buff.clear();
		_currValue = Double.NaN;
	}

	@Override
	public void add(TDateTime time, Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Cannot movavg of a null value");
		}
		if (!(value instanceof Number)) {
			throw new IllegalArgumentException("Cannot movavg non-numeric value (" + value.getClass().getSimpleName() + ")");
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object get() {
		// TODO Auto-generated method stub
		return null;
	}
}
