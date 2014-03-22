package me.akuz.ts.analysis;

import java.util.Date;
import java.util.List;

import me.akuz.ts.TS;
import me.akuz.ts.TSEntry;

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
		
		List<TSEntry<Date>> tsSorted = ts.getSorted();
		for (int i=0; i<tsSorted.size(); i++) {
			
			TSEntry<Date> entry = tsSorted.get(i);
			Date date = entry.getTime();
			
			if (isOn) {
				
				// check if to turn off
				long diff = getDiff(lastDate, date);
				if (diff >= _offPeriodMs) {
					
					// set off
					isOn = false;
					tsIsOn.add(new TSEntry<Date>(lastDate, false));
					
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
						tsIsOn.add(new TSEntry<Date>(date, true));
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
