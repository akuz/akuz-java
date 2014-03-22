package me.akuz.ts.align;

public final class TSAlignFillRepeat extends TSAlignFill {

	private final String _what;
	private final int _maxMissingCount;
	private int _currMissingCount;
	private double _currValue;
	
	public TSAlignFillRepeat(
			final String what,
			final int maxMissingCount) {
		
		_what = what;
		_maxMissingCount = maxMissingCount;
		_currValue = Double.NaN;
	}

	@Override
	public TSAlignLogMsg next(double value) {
		String error = null;
		TSAlignLogMsg msg = null;
		final double prevValue = _currValue;
		_currValue = value;
		if (Double.isNaN(_currValue)) {
			_currMissingCount++;
			if (_currMissingCount <= _maxMissingCount) {
				_currValue = prevValue;
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
