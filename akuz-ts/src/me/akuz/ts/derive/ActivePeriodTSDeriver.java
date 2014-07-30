package me.akuz.ts.derive;

import java.util.Date;
import java.util.List;

import me.akuz.core.Period;
import me.akuz.ts.TSeq;
import me.akuz.ts.TItem;
import me.akuz.ts.TType;

public final class ActivePeriodTSDeriver {
	
	private final long _onPeriodMs;
	private final long _offPeriodMs;
	
	public ActivePeriodTSDeriver(Period onPeriod, Period offPeriod) {
		_onPeriodMs  = onPeriod.getMs();
		_offPeriodMs = offPeriod.getMs();
	}
	
	public TSeq<Date> derive(final TSeq<Date> seq) {
		
		final TSeq<Date> seqActivePeriod = new TSeq<>(TType.BooleanType);

		Date prevDate = null;
		Date lastTurnOnDate = null;
		Date currActiveStretchStartDate = null;
		boolean isActive = false;
		
		final List<TItem<Date>> items = seq.getItems();
		for (int i=0; i<items.size(); i++) {
			
			final TItem<Date> item = items.get(i);
			final Date date = item.getTime();
			
			if (isActive) {
				
				// check if still on
				if (getDistanceMs(prevDate, date) > _offPeriodMs) {
					
					if (lastTurnOnDate != null) {
						
						// check if the active period was non-empty
						if (lastTurnOnDate.compareTo(prevDate) < 0) {
							
							// non-empty active period ended
							seqActivePeriod.acceptStaged();
							seqActivePeriod.add(new TItem<Date>(prevDate, false));
							
						} else {
	
							// empty active period ended
							seqActivePeriod.clearStaged();
						}
					}
					
					isActive = false;
					lastTurnOnDate = null;
					currActiveStretchStartDate = date;
				}
				
			} else {
				
				// check active stretch
				if (prevDate != null) {
					
					// check if there were no items for a long time
					if (getDistanceMs(prevDate, date) > _offPeriodMs) {
						
						// reset active stretch start
						currActiveStretchStartDate = date;
					}
					
				} else {
					
					// initial active stretch start
					currActiveStretchStartDate = date;
				}
				
				// check if time to turn on
				if (getDistanceMs(currActiveStretchStartDate, date) >= _onPeriodMs) {
					
					isActive = true;
					lastTurnOnDate = date;
					seqActivePeriod.stage(date, true);
				}
			}
			
			prevDate = date;
		}
		
		if (prevDate != null) {

			if (lastTurnOnDate != null) {
				
				// check of last active period was non-empty
				if (lastTurnOnDate.compareTo(prevDate) < 0) {
					
					// non-empty active period ended
					seqActivePeriod.acceptStaged();
					seqActivePeriod.add(new TItem<Date>(prevDate, false));
					
				} else {
	
					// empty active period ended
					seqActivePeriod.clearStaged();
				}
			}
		}
		
		return seqActivePeriod;
	}
	
	private final static long getDistanceMs(Date from, Date to) {
		
		long fromMs = from.getTime();
		long toMs = to.getTime();
		
		if (fromMs > toMs) {
			throw new IllegalArgumentException("Dates are not in chronological order");
		}
		
		return toMs - fromMs;
	}

}
