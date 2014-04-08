package me.akuz.ts.align;

import me.akuz.ts.TSItem;

public class TSCheckerNumberJump<T extends Comparable<T>> extends TSChecker<T> {
	
	private final String _logFieldName;
	private final double _ignoreJump;
	private final double _warningJump;
	private final double _errorJump;
	private TSItem<T> _currItem;
	
	public TSCheckerNumberJump( 
			final String logFieldName,
			final double ignoreJump,
			final double warningJump,
			final double errorJump) {
		
		_logFieldName = logFieldName;
		_ignoreJump = ignoreJump;
		_warningJump = warningJump;
		_errorJump = errorJump;
		_currItem = null;
	}

	@Override
	public TSAlignLogMsg next(T time, TSItem<T> item) {
		TSAlignLogMsg msg = null;
		final TSItem<T> prevItem = _currItem;
		_currItem = item;
		if (prevItem != null && _currItem != null) {
			
			double prevNumber = prevItem.getNumber().doubleValue();
			double currNumber = _currItem.getNumber().doubleValue();
			
			final double distance = Math.abs(currNumber - prevNumber);
			if (distance > Double.MIN_NORMAL) {
				
				final double absPrevNumber = Math.abs(prevNumber);
				final double absCurrNumber = Math.abs(currNumber);
				final double norm = Math.min(absPrevNumber, absCurrNumber);
				final double jump = distance / norm;
				if (jump != _ignoreJump) {
					if (jump > _errorJump) {
						msg = new TSAlignLogMsg(TSAlignLogMsgLevel.Error, _logFieldName + " jump: " + prevItem + " -> " + _currItem);
					} else if (jump > _warningJump) {
						msg = new TSAlignLogMsg(TSAlignLogMsgLevel.Warning, _logFieldName + " jump: " + prevItem + " -> " + _currItem);
					}
				}
			}
		}
		return msg;
	}

}
