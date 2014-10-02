package me.akuz.ts.filters.check;

import java.util.Date;
import java.util.List;

import me.akuz.core.TPeriod;
import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;
import me.akuz.ts.log.TLevel;

public class CheckDateGaps extends Filter<Date> {
	
	private final TPeriod _infoAfterPeriod;
	private final TPeriod _warningAfterPeriod;
	private final TPeriod _errorAfterPeriod;
	private TLevel _lastLevel;
	private Date _lastDate;
	
	public CheckDateGaps(
			final TPeriod infoAfterPeriod,
			final TPeriod warningAfterPeriod,
			final TPeriod errorAfterPeriod) {
		
		_infoAfterPeriod = infoAfterPeriod;
		_warningAfterPeriod = warningAfterPeriod;
		_errorAfterPeriod = errorAfterPeriod;
		_lastLevel = TLevel.None;
		_lastDate = null;
	}

	@Override
	public void next(
			final Date time, 
			final SeqCursor<Date> cur,
			final TLog<Date> log) {

		CurrTime.checkNew(_currTime, time);
		
		if (log == null) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " filter requires a log");
		}
		
		/**
		 * Process items moved through time.
		 */
		final List<TItem<Date>> movedItems = cur.getMovedItems();
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
		final TItem<Date> currItem = cur.getCurrItem();
		if (currItem == null && _lastDate != null) {
			checkDateJump(log, _lastDate, time, false);
		}
		
		/**
		 * If no items provided on first call,
		 * start counting the time from the
		 * first call date.
		 */
		if (_lastDate == null) {
			_lastDate = time;
		}
		
		_currTime = time;
	}
	
	private final void checkDateJump(
			final TLog<Date> log, 
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
			if (_lastLevel.compareTo(TLevel.DANGER) < 0) {
				_lastLevel = TLevel.DANGER;
				increasedLevel = true;
			}
		} else if (jump > _warningAfterPeriod.getMs()) {
			if (_lastLevel.compareTo(TLevel.Warning) < 0) {
				_lastLevel = TLevel.Warning;
				increasedLevel = true;
			}
		} else if (jump > _infoAfterPeriod.getMs()) {
			if (_lastLevel.compareTo(TLevel.Info) < 0) {
				_lastLevel = TLevel.Info;
				increasedLevel = true;
			}
		}
		
		if (increasedLevel) {
			log.add(currTime, _lastLevel, "Jump in \"" + getFieldName() + "\" field date: " + prevTime + " >> " + currTime);
		}
		
		if (resetLevel) {
			_lastLevel = TLevel.None;
		}
	}
	
}
