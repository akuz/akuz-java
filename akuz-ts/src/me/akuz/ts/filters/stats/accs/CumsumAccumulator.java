package me.akuz.ts.filters.stats.accs;

public final class CumsumAccumulator<T extends Comparable<T>> extends Accumulator<T> {

	private final double _startValue;
	private double _currValue;
	
	public CumsumAccumulator() {
		_startValue = 0.0;
		_currValue = 0.0;
	}
	
	public CumsumAccumulator(final double startValue) {
		_startValue = startValue;
		_currValue = startValue;
	}
	
	@Override
	public void reset() {
		_currValue = _startValue;
	}

	@Override
	public void add(final T time, final Object value) {
		if (value != null) {
			if (!(value instanceof Number)) {
				throw new IllegalArgumentException("Cannot cumsum non-numeric value (" + value.getClass().getSimpleName() + ")");
			}
			_currValue += ((Number)value).doubleValue();
		}
	}
	
	public Object get() {
		return _currValue;
	}

}
