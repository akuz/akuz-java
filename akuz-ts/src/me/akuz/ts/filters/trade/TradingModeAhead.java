package me.akuz.ts.filters.trade;

import java.util.Date;

import me.akuz.core.Period;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

/**
 * Look-ahead trading mode filter.
 *
 */
public final class TradingModeAhead extends Filter<Date> {
	
	public static final Integer TRADING    = 3;
	public static final Integer SOFT_EXIT  = 2;
	public static final Integer HARD_EXIT  = 1;
	public static final Integer NO_TRADING = 0;
	
	private final HasTimeGapsAhead _hasGapsSoft;
	private final HasTimeGapsAhead _hasGapsHard;
	private final CanExecuteAhead _canExecute;
	
	private TItem<Date> _currItem;
	
	public TradingModeAhead(
			final Period gapOkPeriod,
			final Period hardExitPeriod,
			final Period softExitPeriod,
			final Period minExecutePeriod,
			final Period maxExecutePeriod) {
		
		if (hardExitPeriod.getMs() >= softExitPeriod.getMs()) {
			throw new IllegalArgumentException("Hard exit period must be shorted than soft exit period");
		}
		
		_hasGapsHard = new HasTimeGapsAhead(gapOkPeriod, hardExitPeriod);
		_hasGapsSoft = new HasTimeGapsAhead(gapOkPeriod, softExitPeriod);
		_canExecute = new CanExecuteAhead(minExecutePeriod, maxExecutePeriod);
	}

	@Override
	public void next(
			final TLog log, 
			final Date currTime, 
			final SeqIterator<Date> iter) {
		
		_hasGapsHard.next(log, currTime, iter);
		_hasGapsSoft.next(log, currTime, iter);
		_canExecute.next(log, currTime, iter);
		
		final boolean hasGapsHard = _hasGapsHard.getCurrItem().getBoolean();
		final boolean hasGapsSoft = _hasGapsSoft.getCurrItem().getBoolean();
		final boolean canExecute = _canExecute.getCurrItem().getBoolean();
		
		if (hasGapsHard) {
			if (canExecute) {
				_currItem = new TItem<Date>(currTime, HARD_EXIT);
			} else {
				_currItem = new TItem<Date>(currTime, NO_TRADING);
			}
		} else if (hasGapsSoft) {
			if (canExecute) {
				_currItem = new TItem<Date>(currTime, SOFT_EXIT);
			} else {
				_currItem = new TItem<Date>(currTime, NO_TRADING);
			}
		} else if (canExecute) {
			_currItem = new TItem<Date>(currTime, TRADING);
		} else {
			_currItem = new TItem<Date>(currTime, NO_TRADING);
		}
	}

	@Override
	public TItem<Date> getCurrItem() {
		return _currItem;
	}

}
