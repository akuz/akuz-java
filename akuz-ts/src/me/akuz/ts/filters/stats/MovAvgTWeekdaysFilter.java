package me.akuz.ts.filters.stats;

import me.akuz.core.TDate;
import me.akuz.ts.filters.stats.accs.MovAvgTWeekdaysAccumulator;

public final class MovAvgTWeekdaysFilter extends AccumulatorFilter<TDate> {
	
	public MovAvgTWeekdaysFilter(
			final int sampleCount, 
			final int gapOkWeekdays) {
		
		super(new MovAvgTWeekdaysAccumulator(
				sampleCount,
				gapOkWeekdays));
	}

}
