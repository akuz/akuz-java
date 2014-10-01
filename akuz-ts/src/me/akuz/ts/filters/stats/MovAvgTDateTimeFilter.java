package me.akuz.ts.filters.stats;

import me.akuz.core.TDateTime;
import me.akuz.core.TPeriod;
import me.akuz.ts.filters.stats.accs.MovAvgTDateTimeAccumulator;

public final class MovAvgTDateTimeFilter extends AccumulatorFilter<TDateTime> {
	
	public MovAvgTDateTimeFilter(
			final int sampleCount, 
			final TPeriod gapOkDur) {
		
		super(new MovAvgTDateTimeAccumulator(
				sampleCount,
				gapOkDur));
	}

}
