package me.akuz.ts.filters.simple;

import java.util.Date;
import java.util.List;

import me.akuz.core.Period;
import me.akuz.ts.TItem;
import me.akuz.ts.filters.TFilter;
import me.akuz.ts.log.TLog;
import me.akuz.ts.log.TLogLevel;

public class CheckDateGaps extends TFilter<Date> {
	
	private String _fieldName;
	private final Period _infoAfterPeriod;
	private final Period _warningAfterPeriod;
	private final Period _errorAfterPeriod;
	private TLogLevel _lastLevel;
	private Date _lastDate;
	
	public CheckDateGaps(
			final Period infoAfterPeriod,
			final Period warningAfterPeriod,
			final Period errorAfterPeriod) {
		
		_fieldName = "unspecified";
		_infoAfterPeriod = infoAfterPeriod;
		_warningAfterPeriod = warningAfterPeriod;
		_errorAfterPeriod = errorAfterPeriod;
		_lastLevel = TLogLevel.None;
		_lastDate = null;
	}
	
	@Override
	public void setFieldName(final String fieldName) {
		_fieldName = fieldName;
	}
	
	@Override
	public TItem<Date> getCurrent() {
		// we are only checking for
		// jumps, but we don't
		// derive any state
		return null;
	}

	@Override
	public void next(
			TLog log,
			Date currTime, 
			TItem<Date> currItem,
			List<TItem<Date>> movedItems) {
		
		if (log == null) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " filter requires a log");
		}
		
		/**
		 * Process items moved through time.
		 */
		for (int i=0; i<movedItems.size(); i++) {
			
			final Date prevDate = _lastDate;
			_lastDate = movedItems.get(i).getTime();
			
			if (prevDate != null) {
				checkDateJump(log, prevDate, _lastDate, true);
			}
		}
		
		/**
		 * If no item at current time, and we
		 * already saw some previous items, then
		 * check the date jump from last item to now.
		 */
		if (currItem == null && _lastDate != null) {
			checkDateJump(log, _lastDate, currTime, false);
		}
		
		/**
		 * If no items provided on first call,
		 * start counting the time from the
		 * first call date.
		 */
		if (_lastDate == null) {
			_lastDate = currTime;
		}
	}
	
	private final void checkDateJump(
			final TLog log, 
			final Date prevTime,
			final Date currTime,
			final boolean resetLevel) {
		
		final long jump = currTime.getTime() - prevTime.getTime();
		
		if (jump <= 0) {
			throw new IllegalStateException("Times are not in chronological order: " +
											prevTime + " must be < than " + currTime);
		}

		boolean increasedLevel = false;
		
		if (jump > _errorAfterPeriod.getMs()) {
			if (_lastLevel.compareTo(TLogLevel.Error) < 0) {
				_lastLevel = TLogLevel.Error;
				increasedLevel = true;
			}
		} else if (jump > _warningAfterPeriod.getMs()) {
			if (_lastLevel.compareTo(TLogLevel.Warning) < 0) {
				_lastLevel = TLogLevel.Warning;
				increasedLevel = true;
			}
		} else if (jump > _infoAfterPeriod.getMs()) {
			if (_lastLevel.compareTo(TLogLevel.Info) < 0) {
				_lastLevel = TLogLevel.Info;
				increasedLevel = true;
			}
		}
		
		if (increasedLevel) {
			log.add(_lastLevel, "Jump in \"" + _fieldName + "\" date: " + prevTime + " >> " + currTime);
		}
		
		if (resetLevel) {
			_lastLevel = TLogLevel.None;
		}
	}
	
}
