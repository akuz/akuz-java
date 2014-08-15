package me.akuz.ts.filters.trade;

import java.util.Date;

import me.akuz.core.Period;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.filters.Filter;
import me.akuz.ts.log.TLog;

public final class TradeModeAhead extends Filter<Date> {
	
	public static final String TRADING     = "t";
	public static final String KEEP_POS    = "k";
	public static final String TRADE_OUT   = "o";
	public static final String NOT_TRADING = "n";
	
	public TradeModeAhead(Period period) {
		
	}

	@Override
	public void next(
			final TLog log, 
			final Date currTime, 
			final SeqIterator<Date> iter) {
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public TItem<Date> getCurrItem() {
		
		// TODO Auto-generated method stub
		return null;
	}

}
