package me.akuz.ts.filters.trade;

import java.util.Date;
import java.util.List;

import me.akuz.core.DateUtils;
import me.akuz.core.Period;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class TradeModeAhead extends Filter<Date> {
	
	public static final String TRADING         = "t";
	public static final String KEEP_POSITION   = "k";
	public static final String SOFT_TRADE_OUT  = "s";
	public static final String HARD_TRADE_OUT  = "h";
	public static final String NOT_TRADING     = "-";
	
	private final Period _execPeriod;
	private final Period _softPeriod;
	private final Period _longPeriod;
	
	public TradeModeAhead(
			final Period execPeriod,
			final Period softPeriod,
			final Period longPeriod) {
		
		if (execPeriod.getMs() >= softPeriod.getMs()) {
			throw new IllegalArgumentException(
					"Hard period must be shorter than soft period");
		}
		if (softPeriod.getMs() >= longPeriod.getMs()) {
			throw new IllegalArgumentException(
					"Soft period must be shorter than long period");
		}
		_execPeriod = execPeriod;
		_softPeriod = softPeriod;
		_longPeriod = longPeriod;
	}

	@Override
	public void next(
			final TLog log, 
			final Date currTime, 
			final SeqIterator<Date> iter) {
		
		final List<TItem<Date>> items = iter.getSeq().getItems();
		
		final Date hardDeadline = DateUtils.addDays(currTime, _execPeriod.getDays());
		final Date softDeadline = DateUtils.addDays(currTime, _softPeriod.getDays());
		final Date longDeadline = DateUtils.addDays(currTime, _longPeriod.getDays());
		
		boolean hasItemsWithinHardPeriod = false;
		boolean hasItemsWithinSoftPeriod = false;
		boolean hasItemsWithinLongPeriod = false;
		{
			int cursor = iter.getNextCursor();
			while (cursor < items.size()) {
				
				final TItem<Date> item = items.get(cursor);
				
				if (item.getTime().compareTo(hardDeadline) <= 0) {
					hasItemsWithinHardPeriod = true;
					continue;
				} else if (item.getTime().compareTo(softDeadline) <= 0) {
					hasItemsWithinSoftPeriod = true;
					continue;
				} else if (item.getTime().compareTo(longDeadline) <= 0) {
					hasItemsWithinLongPeriod = true;
					break;
				} else {
					break;
				}
			}
		}
		
		final String tradeMode;
		
		if (hasItemsWithinHardPeriod && 
			hasItemsWithinSoftPeriod &&
			hasItemsWithinLongPeriod) {
			
			tradeMode = TRADING;
			
		} else if (
			hasItemsWithinHardPeriod &&
			hasItemsWithinSoftPeriod) {
			
			tradeMode = SOFT_TRADE_OUT;
			
		} else if (
			hasItemsWithinHardPeriod) {
			
			tradeMode = HARD_TRADE_OUT;
			
		} else {
			
			tradeMode = NOT_TRADING;
		}
		
		new TItem<>(currTime, tradeMode);
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public TItem<Date> getCurrItem() {
		
		// TODO Auto-generated method stub
		return null;
	}

}
