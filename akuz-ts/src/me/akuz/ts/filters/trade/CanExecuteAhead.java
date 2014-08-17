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
 * Look-ahead filter indicating whether it's possible to execute a trade by 
 * looking if there are future items (prices) between min and max time ahead.
 *
 */
public final class CanExecuteAhead extends Filter<Date> {
	
	private final Period _minPeriod;
	private final Period _maxPeriod;
	
	private TItem<Date> _currItem;
	
	public CanExecuteAhead(
			final Period minPeriod,
			final Period maxPeriod) {
		
		if (minPeriod.getMs() < 0) {
			throw new IllegalArgumentException("Ahead min period must be non-negative");
		}
		if (minPeriod.getMs() > maxPeriod.getMs()) {
			throw new IllegalArgumentException("Ahead min period must be <= max period");
		}
		_minPeriod = minPeriod;
		_maxPeriod = maxPeriod;
	}

	@Override
	public void next(final TLog log, final Date currTime, final SeqIterator<Date> iter) {
		
		if (_minPeriod.getMs() == 0 && iter.getCurrItem() != null) {
			_currItem = new TItem<Date>(currTime, true);
			return;
		}

		final Date minTime = DateUtils.addMs(currTime, _minPeriod.getMs());
		final Date maxTime = DateUtils.addMs(currTime, _maxPeriod.getMs());
		final List<TItem<Date>> items = iter.getSeq().getItems();
		int cursor = iter.getNextCursor();
		while (cursor < items.size()) {
			
			final TItem<Date> loopItem = items.get(cursor);
			
			// check if already in the future ahead of max time
			if (DateUtils.msBetween(loopItem.getTime(), maxTime) > 0) {
				_currItem = new TItem<Date>(currTime, false);
				return;
			}
			
			// check if found item between min and max time
			if (DateUtils.msBetween(minTime, loopItem.getTime()) >= 0 &&
				DateUtils.msBetween(loopItem.getTime(), maxTime) <= 0) {
				
				_currItem = new TItem<Date>(currTime, true);
				return;
			}
			
			cursor++;
		}
		
		// could not find item between min and max time
		_currItem = new TItem<Date>(currTime, false);
	}

	@Override
	public TItem<Date> getCurrItem() {
		return _currItem;
	}

}
