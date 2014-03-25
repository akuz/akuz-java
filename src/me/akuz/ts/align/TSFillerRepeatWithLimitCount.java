package me.akuz.ts.align;

import me.akuz.ts.TSItem;

public final class TSFillerRepeatWithLimitCount<T extends Comparable<T>> extends TSFiller<T> {

	private final String _logFieldName;
	private final int _maxMissingCount;
	private int _currMissingCount;
	private TSItem<T> _currItem;
	
	public TSFillerRepeatWithLimitCount(
			final String logFieldName,
			final int maxMissingCount) {
		
		_logFieldName = logFieldName;
		_maxMissingCount = maxMissingCount;
		_currItem = null;
	}

	@Override
	public TSAlignLogMsg next(T time, TSItem<T> item) {
		String error = null;
		TSAlignLogMsg msg = null;
		final TSItem<T> prevItem = _currItem;
		_currItem = item;
		if (_currItem == null) {
			_currMissingCount++;
			if (_maxMissingCount <= 0 ||
				_currMissingCount <= _maxMissingCount) {
				if (prevItem != null) {
					_currItem = new TSItem<T>(time, prevItem.getObject());
				}
			} else {
				error = "could not fill, missing values count exceeded max of " + _maxMissingCount;
			}
		} else {
			_currMissingCount = 0;
		}
		if (_currItem == null) {
			msg = new TSAlignLogMsg(TSAlignLogMsgLevel.Error, _logFieldName + " value is missing (" + error + ")");
		}
		return msg;
	}

	@Override
	public TSItem<T> getCurrent() {
		return _currItem;
	}

}
