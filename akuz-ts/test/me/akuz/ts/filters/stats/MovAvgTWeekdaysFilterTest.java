package me.akuz.ts.filters.stats;

import me.akuz.core.TDateTime;
import me.akuz.core.TPeriod;
import me.akuz.ts.Seq;
import me.akuz.ts.SeqFilter;

import org.junit.Assert;
import org.junit.Test;

public class MovAvgTWeekdaysFilterTest {

	@Test
	public void testSimple2() {
		
		Seq<TDateTime> seq = new Seq<>();
		
		seq.add(new TDateTime(2013, 1, 2),  2.5);
		seq.add(new TDateTime(2013, 1, 3),  1.5);
		seq.add(new TDateTime(2013, 1, 5), -1.0);
		seq.add(new TDateTime(2013, 2, 7),  1.5);
		seq.add(new TDateTime(2013, 2, 9), 0.5);
		
		SeqFilter<TDateTime> filter
			= new SeqFilter<>(seq.iterator())
				.addFilter(new MovAvgTDateTimeFilter(2, TPeriod.fromDays(2.01)));
		
		filter.moveToTime(new TDateTime(2013, 1, 1));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDateTime(2013, 1, 2));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDateTime(2013, 1, 3));
		Assert.assertEquals(2.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 1, 4));
		Assert.assertEquals(2.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 1, 5));
		Assert.assertEquals(0.25, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 1, 6));
		Assert.assertEquals(0.25, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 1, 7));
		Assert.assertEquals(0.25, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 1, 8));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDateTime(2013, 2, 6));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDateTime(2013, 2, 7));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDateTime(2013, 2, 9));
		Assert.assertEquals(1.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 2, 10));
		Assert.assertEquals(1.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 2, 11));
		Assert.assertEquals(1.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 2, 12));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

	}

	@Test
	public void testSimple3() {
		
		Seq<TDateTime> seq = new Seq<>();
		
		seq.add(new TDateTime(2013, 1, 2),  2.5);
		seq.add(new TDateTime(2013, 1, 3),  1.5);
		seq.add(new TDateTime(2013, 1, 5), -1.0);
		seq.add(new TDateTime(2013, 1, 6), -0.5);
		
		SeqFilter<TDateTime> filter
			= new SeqFilter<>(seq.iterator())
				.addFilter(new MovAvgTDateTimeFilter(3, TPeriod.fromDays(2.01)));
		
		filter.moveToTime(new TDateTime(2013, 1, 1));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDateTime(2013, 1, 2));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDateTime(2013, 1, 3));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDateTime(2013, 1, 4));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDateTime(2013, 1, 5));
		Assert.assertEquals(1.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 1, 6));
		Assert.assertEquals(0.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 1, 7));
		Assert.assertEquals(0.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 1, 8));
		Assert.assertEquals(0.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDateTime(2013, 1, 9));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

	}

}
