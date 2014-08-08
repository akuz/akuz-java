package me.akuz.ts.derive;

import java.util.Date;
import java.util.List;

import me.akuz.core.Period;
import me.akuz.ts.Seq;
import me.akuz.ts.TItem;

public final class IsActive {
	
	public static Seq<Date> calc(
			final Seq<Date> seq,
			final Period onPeriod,
			final Period offPeriod) {
		
		final long onPeriodMs = onPeriod.getMs();
		final long offPeriodMs = offPeriod.getMs();
		final Seq<Date> seqActive = new Seq<>();

		Date prevDate = null;
		Date lastTurnOnDate = null;
		Date currActiveStartDate = null;
		boolean isActive = false;
		
		final List<TItem<Date>> items = seq.getItems();
		for (int i=0; i<items.size(); i++) {
			
			final TItem<Date> item = items.get(i);
			final Date date = item.getTime();
			
			if (isActive) {
				
				// check if still on
				if (getDistanceMs(prevDate, date) > offPeriodMs) {
					
					if (lastTurnOnDate != null) {
						
						// check if the active period was non-empty
						if (lastTurnOnDate.compareTo(prevDate) < 0) {
							
							// non-empty active period ended
							seqActive.acceptStaged();
							seqActive.add(new TItem<Date>(prevDate, false));
							
						} else {
	
							// empty active period ended
							seqActive.clearStaged();
						}
					}
					
					isActive = false;
					lastTurnOnDate = null;
					currActiveStartDate = date;
				}
				
			} else {
				
				// check active stretch
				if (prevDate != null) {
					
					// check if there were no items for a long time
					if (getDistanceMs(prevDate, date) > offPeriodMs) {
						
						// reset active stretch start
						currActiveStartDate = date;
					}
					
				} else {
					
					// initial active stretch start
					currActiveStartDate = date;
				}
				
				// check if time to turn on
				if (getDistanceMs(currActiveStartDate, date) >= onPeriodMs) {
					
					isActive = true;
					lastTurnOnDate = date;
					seqActive.stage(date, true);
				}
			}
			
			prevDate = date;
		}
		
		if (prevDate != null) {

			if (lastTurnOnDate != null) {
				
				// check of last active period was non-empty
				if (lastTurnOnDate.compareTo(prevDate) < 0) {
					
					// non-empty active period ended
					seqActive.acceptStaged();
					seqActive.add(new TItem<Date>(prevDate, false));
					
				} else {
	
					// empty active period ended
					seqActive.clearStaged();
				}
			}
		}
		
		return seqActive;
	}
	
	private final static long getDistanceMs(
			final Date fromDate, 
			final Date toDate) {
		
		final long fromMs = fromDate.getTime();
		final long toMs = toDate.getTime();
		
		if (fromMs > toMs) {
			throw new IllegalArgumentException("Dates are not in chronological order");
		}
		
		return toMs - fromMs;
	}

}
