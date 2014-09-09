package me.akuz.ts.filters.stats;

import me.akuz.core.TDate;
import me.akuz.ts.Seq;
import me.akuz.ts.SeqFilter;

import org.junit.Assert;
import org.junit.Test;

public class CumsumFilterTest {

	// FIXME multidimensional test

	@Test
	public void testSimple() {
		
		Seq<TDate> seq = new Seq<>();
		
		seq.add(new TDate(2013, 1, 2),  2.5);
		seq.add(new TDate(2013, 1, 3),  1.5);
		seq.add(new TDate(2013, 1, 5), -1.0);
		seq.add(new TDate(2013, 2, 1),  2.0);
		seq.add(new TDate(2013, 2, 7),  0.5);
		
		SeqFilter<TDate> filter
			= new SeqFilter<>(seq.iterator())
				.addFilter(new CumsumFilter<TDate>());
		
		filter.moveToTime(new TDate(2013, 1, 1));
		Assert.assertEquals(0.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 2));
		Assert.assertEquals(2.5, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 3));
		Assert.assertEquals(4.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 4));
		Assert.assertEquals(4.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 5));
		Assert.assertEquals(3.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 6));
		Assert.assertEquals(3.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 1, 7));
		Assert.assertEquals(3.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 2, 1));
		Assert.assertEquals(5.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 2, 2));
		Assert.assertEquals(5.0, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 2, 7));
		Assert.assertEquals(5.5, filter.getCurrItem().getDouble(), 0.00000001);

		filter.moveToTime(new TDate(2013, 2, 9));
		Assert.assertEquals(5.5, filter.getCurrItem().getDouble(), 0.00000001);
	}

}
