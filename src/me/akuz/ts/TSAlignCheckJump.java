package me.akuz.ts;

public class TSAlignCheckJump extends TSAlignCheck {
	
	private final String _what;
	private final double _ignoreJump;
	private final double _warningJump;
	private final double _errorJump;
	private double _currValue;
	
	public TSAlignCheckJump( 
			final String what,
			final double ignoreJump,
			final double warningJump,
			final double errorJump) {
		
		_what = what;
		_ignoreJump = ignoreJump;
		_warningJump = warningJump;
		_errorJump = errorJump;
		_currValue = Double.NaN;
	}

	@Override
	public TSAlignLogMsg next(double value) {
		TSAlignLogMsg msg = null;
		final double prevValue = _currValue;
		_currValue = value;
		if (!Double.isNaN(prevValue) && !Double.isNaN(_currValue)) {
			final double distance = Math.abs(_currValue - prevValue);
			if (distance > Double.MIN_NORMAL) {
				final double absPrevValue = Math.abs(prevValue);
				final double absCurrValue = Math.abs(_currValue);
				final double norm = Math.min(absPrevValue, absCurrValue);
				final double jump = distance / norm;
				if (jump != _ignoreJump) {
					if (jump > _errorJump) {
						msg = new TSAlignLogMsg(TSAlignLogMsgLevel.Error, _what + " jump: " + prevValue + " -> " + _currValue);
					} else if (jump > _warningJump) {
						msg = new TSAlignLogMsg(TSAlignLogMsgLevel.Warning, _what + " jump: " + prevValue + " -> " + _currValue);
					}
				}
			}
		}
		return msg;
	}

}
