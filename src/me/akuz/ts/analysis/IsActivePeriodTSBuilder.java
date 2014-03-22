package me.akuz.ts.analysis;

import java.util.Date;
import java.util.List;

import me.akuz.ts.TS;
import me.akuz.ts.TSItem;

public final class IsActivePeriodTSBuilder {
	
	private final long _onPeriodMs;
	private final long _offPeriodMs;
	
	public IsActivePeriodTSBuilder(long onPeriodMs, long offPeriodMs) {
		_onPeriodMs = onPeriodMs;
		_offPeriodMs = offPeriodMs;
	}
	
	public TS<Date> build(TS<Date> ts) {
		
		TS<Date> tsIsOn = new TS<>();

		boolean isOn = false;
		Date lastDate = null;
		Date lastActiveOffStretchStartDate = null;
		
		List<TSItem<Date>> tsSorted = ts.getItems();
		for (int i=0; i<tsSorted.size(); i++) {
			
			TSItem<Date> entry = tsSorted.get(i);
			Date date = entry.getTime();
			
			if (isOn) {
				
				// check if to turn off
				long diff = getDiff(lastDate, date);
				if (diff >= _offPeriodMs) {
					
					// set off
					isOn = false;
					tsIsOn.add(new TSItem<Date>(lastDate, false));
					
					// reset active off stretch start
					lastActiveOffStretchStartDate = date;
				}
				
			} else {
				
				// check off stretch
				if (lastDate != null) {
					long diff = getDiff(lastDate, date);
					if (diff >= _offPeriodMs) {
						// reset active off stretch start
						lastActiveOffStretchStartDate = date;
					}
				} else {
					// initial active off stretch start
					lastActiveOffStretchStartDate = date;
				}
				
				// check if to turn on
				if (lastActiveOffStretchStartDate != date) {
					long diff = getDiff(lastActiveOffStretchStartDate, date);
					if (diff >= _onPeriodMs) {
						
						// set on
						isOn = true;
						tsIsOn.add(new TSItem<Date>(date, true));
					}
				}
			}
			
			lastDate = date;
		}
		
		return tsIsOn;
	}
	
	private final static long getDiff(Date from, Date to) {
		
		long fromMs = from.getTime();
		long toMs = to.getTime();
		
		if (fromMs > toMs) {
			throw new IllegalArgumentException("Dates are not in chronological order");
		}
		
		return toMs - fromMs;
	}

}
