package me.akuz.ts.build;

import java.util.Date;
import java.util.List;

import me.akuz.core.Period;
import me.akuz.ts.TS;
import me.akuz.ts.TSItem;

public final class ActivePeriodTSBuilder {
	
	private final long _onPeriodMs;
	private final long _offPeriodMs;
	
	public ActivePeriodTSBuilder(Period onPeriod, Period offPeriod) {
		_onPeriodMs  = onPeriod.getMs();
		_offPeriodMs = offPeriod.getMs();
	}
	
	public TS<Date> build(final TS<Date> ts) {
		
		final TS<Date> tsActivePeriod = new TS<>();

		Date prevDate = null;
		Date currActiveStretchStartDate = null;
		boolean isActive = false;
		
		final List<TSItem<Date>> tsSorted = ts.getItems();
		for (int i=0; i<tsSorted.size(); i++) {
			
			final TSItem<Date> item = tsSorted.get(i);
			final Date date = item.getTime();
			
			if (isActive) {
				
				// check if still on
				long diff = getDistanceMs(prevDate, date);
				if (diff > _offPeriodMs) {
					
					TSItem<Date> lastIsOnItem = tsActivePeriod.getLast();
					if (lastIsOnItem != null) {
						if (lastIsOnItem.getTime().compareTo(prevDate) < 0) {
							
							// non-empty active period ended
							tsActivePeriod.add(new TSItem<Date>(prevDate, false));
							
						} else {
	
							// empty active period ended
							tsActivePeriod.removeLast();
						}
					}
					
					currActiveStretchStartDate = date;
					isActive = false;
				}
				
			} else {
				
				// check active stretch
				if (prevDate != null) {
					long diff = getDistanceMs(prevDate, date);
					if (diff > _offPeriodMs) {
						
						// reset active stretch start
						currActiveStretchStartDate = date;
					}
					
				} else {
					
					// initial active stretch start
					currActiveStretchStartDate = date;
				}
				
				// check if still off
				long diff = getDistanceMs(currActiveStretchStartDate, date);
				if (diff >= _onPeriodMs) {
					
					// set on
					isActive = true;
					tsActivePeriod.add(new TSItem<Date>(date, true));
				}
			}
			
			prevDate = date;
		}
		
		if (prevDate != null) {

			TSItem<Date> lastIsOnItem = tsActivePeriod.getLast();
			if (lastIsOnItem != null && lastIsOnItem.getBoolean()) {
				if (lastIsOnItem.getTime().compareTo(prevDate) < 0) {
					
					// non-empty active period ended
					tsActivePeriod.add(new TSItem<Date>(prevDate, false));
					
				} else {
	
					// empty active period ended
					tsActivePeriod.removeLast();
				}
			}
		}
		
		return tsActivePeriod;
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
