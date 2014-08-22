package me.akuz.ts.derive;

import java.util.List;

import me.akuz.core.DateAK;
import me.akuz.ts.Seq;
import me.akuz.ts.TItem;

import org.joda.time.Days;

public final class IsActive {
	
	public static Seq<DateAK> calc(
			final Seq<DateAK> seq,
			final Days onAfterDays,
			final Days offAfterDays) {
		
		final Seq<DateAK> seqActive = new Seq<>();

		DateAK prevDate = null;
		DateAK lastTurnOnDate = null;
		DateAK currActiveStartDate = null;
		boolean isActive = false;
		
		final List<TItem<DateAK>> items = seq.getItems();
		for (int i=0; i<items.size(); i++) {
			
			final TItem<DateAK> item = items.get(i);
			final DateAK date = item.getTime();
			
			if (isActive) {
				
				// check if still on
				final Days days = Days.daysBetween(prevDate.get(), date.get());
				if (days.compareTo(offAfterDays) > 0) {
					
					if (lastTurnOnDate != null) {
						
						// check if the active period was non-empty
						if (lastTurnOnDate.compareTo(prevDate) < 0) {
							
							// non-empty active period ended
							seqActive.acceptStaged();
							seqActive.add(new TItem<DateAK>(prevDate, false));
							
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
					final Days days = Days.daysBetween(prevDate.get(), date.get());
					if (days.compareTo(offAfterDays) > 0) {
						
						// reset active stretch start
						currActiveStartDate = date;
					}
					
				} else {
					
					// initial active stretch start
					currActiveStartDate = date;
				}
				
				// check if time to turn on
				final Days days = Days.daysBetween(currActiveStartDate.get(), date.get());
				if (days.compareTo(onAfterDays) >= 0) {
					
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
					seqActive.add(new TItem<DateAK>(prevDate, false));
					
				} else {
	
					// empty active period ended
					seqActive.clearStaged();
				}
			}
		}
		
		return seqActive;
	}

}
