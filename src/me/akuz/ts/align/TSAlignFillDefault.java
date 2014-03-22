package me.akuz.ts.align;

public class TSAlignFillDefault extends TSAlignFill {
	
	private final String _what;
	private final int _maxMissingCount;
	private final double _defaultValue;
	private int _currMissingCount;
	private double _currValue;
	
	public TSAlignFillDefault(
			final String what,
			final int maxMissingCount,
			final double defaultValue) {
		
		_what = what;
		_maxMissingCount = maxMissingCount;
		_defaultValue = defaultValue;
		_currValue = Double.NaN;
	}

	@Override
	public TSAlignLogMsg next(double value) {
		String error = null;
		TSAlignLogMsg msg = null;
		_currValue = value;
		if (Double.isNaN(_currValue)) {
			_currMissingCount++;
			if (_currMissingCount <= _maxMissingCount) {
				_currValue = _defaultValue;
			} else {
				error = "Missing values count exceeded max of " + _maxMissingCount;
			}
		} else {
			_currMissingCount = 0;
		}
		if (Double.isNaN(_currValue)) {
			msg = new TSAlignLogMsg(TSAlignLogMsgLevel.Error, _what + " is NaN (" + error + ")");
		}
		return msg;
	}

	@Override
	public double get() {
		return _currValue;
	}

}
