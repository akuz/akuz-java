package me.akuz.ts.filters.stats;

import me.akuz.core.TDate;
import me.akuz.ts.Seq;
import me.akuz.ts.SeqFilter;

import org.junit.Assert;
import org.junit.Test;

public class MovAvgTDateTimeFilterTest {

	@Test
	public void testSimple2() {
		
		Seq<TDate> seq = new Seq<>();
		
		seq.add(new TDate(2013, 1, 2),  2.5);
		seq.add(new TDate(2013, 1, 3),  1.5);
		seq.add(new TDate(2013, 1, 7), -1.0);
		seq.add(new TDate(2013, 2, 7),  1.5);
		seq.add(new TDate(2013, 2, 11), 0.5);
		
		SeqFilter<TDate> filter
			= new SeqFilter<>(seq.iterator())
				.addFilter(new MovAvgTWeekdaysFilter(2, 2));
		
		filter.moveToTime(new TDate(2013, 1, 1));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDate(2013, 1, 2));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDate(2013, 1, 3));
		Assert.assertEquals(2.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 4));
		Assert.assertEquals(2.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 7));
		Assert.assertEquals(0.25, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 8));
		Assert.assertEquals(0.25, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 9));
		Assert.assertEquals(0.25, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 10));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDate(2013, 2, 6));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDate(2013, 2, 7));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDate(2013, 2, 11));
		Assert.assertEquals(1.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 2, 12));
		Assert.assertEquals(1.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 2, 13));
		Assert.assertEquals(1.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 2, 14));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

	}

	@Test
	public void testSimple3() {
		
		Seq<TDate> seq = new Seq<>();
		
		seq.add(new TDate(2013, 1, 2),  2.5);
		seq.add(new TDate(2013, 1, 3),  1.5);
		seq.add(new TDate(2013, 1, 7), -1.0);
		seq.add(new TDate(2013, 1, 8), -0.5);
		
		SeqFilter<TDate> filter
			= new SeqFilter<>(seq.iterator())
				.addFilter(new MovAvgTWeekdaysFilter(3, 2));
		
		filter.moveToTime(new TDate(2013, 1, 1));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDate(2013, 1, 2));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDate(2013, 1, 3));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDate(2013, 1, 4));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

		filter.moveToTime(new TDate(2013, 1, 7));
		Assert.assertEquals(1.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 8));
		Assert.assertEquals(0.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 9));
		Assert.assertEquals(0.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 10));
		Assert.assertEquals(0.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 11));
		Assert.assertTrue(Double.isNaN(filter.getCurrItem().getDouble()));

	}

}
