package me.akuz.ts.filters.trade;

import java.util.Date;
import java.util.List;

import me.akuz.core.DateUtils;
import me.akuz.core.Period;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

/**
 * Look-ahead filter that check if there are time gaps ahead.
 *
 */
public final class HasTimeGapsAhead extends Filter<Date> {
	
	private final Period _gapOkPeriod;
	private final Period _checkPeriod;
	private TItem<Date> _currItem;
	
	public HasTimeGapsAhead(final Period gapOkPeriod, final Period checkPeriod) {
		
		if (gapOkPeriod.getMs() <= 0) {
			throw new IllegalArgumentException("Gap OK period must be positive");
		}
		if (checkPeriod.getMs() < 0) {
			throw new IllegalArgumentException("Ahead period must be non-negative");
		}
		_gapOkPeriod = gapOkPeriod;
		_checkPeriod = checkPeriod;
	}

	@Override
	public void next(TLog log, Date currTime, SeqIterator<Date> iter) {
		
		final Date maxAheadTime = DateUtils.addMs(currTime, _checkPeriod.getMs());
		final List<TItem<Date>> items = iter.getSeq().getItems();
		int cursor = iter.getNextCursor();
		Date loopTime = currTime;
		while (true) {
			
			final Date nextTime;
			if (cursor < items.size()) {
				nextTime = items.get(cursor).getTime();
			} else {
				nextTime = null;
			}
			
			// check if gap
			if (nextTime == null ||
				DateUtils.msBetween(loopTime, nextTime) > _gapOkPeriod.getMs()) {
				
				// found a gap in time
				_currItem = new TItem<Date>(currTime, true);
				return;
			}
			
			// determine if we need to stop checking
			if (loopTime.compareTo(maxAheadTime) >= 0) {
				_currItem = new TItem<Date>(currTime, false);
				return;
			}
			
			// next time is not null here
			loopTime = nextTime;
			cursor++;
		}
	}

	@Override
	public TItem<Date> getCurrItem() {
		return _currItem;
	}

}
