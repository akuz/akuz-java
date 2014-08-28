package me.akuz.core;

import org.junit.Assert;
import org.junit.Test;

public class TWeekdaysTest {
	
	@Test
	public void testSimple() {
		
		// checkWeekday
		TWeekdays.checkWeekday(new TDate(20140825));
		TWeekdays.checkWeekday(new TDate(20140826));
		TWeekdays.checkWeekday(new TDate(20140827));
		TWeekdays.checkWeekday(new TDate(20140828));
		TWeekdays.checkWeekday(new TDate(20140829));
		try {
			TWeekdays.checkWeekday(new TDate(20140830));
			Assert.fail("Should have thrown exception because the date is not a weekday");
		} catch (IllegalArgumentException ex) {
			// expected
		}
		try {
			TWeekdays.checkWeekday(new TDate(20140831));
			Assert.fail("Should have thrown exception because the date is not a weekday");
		} catch (IllegalArgumentException ex) {
			// expected
		}
		
		// isWeekday
		Assert.assertTrue(TWeekdays.isWeekday(new TDate(20140825)));
		Assert.assertTrue(TWeekdays.isWeekday(new TDate(20140826)));
		Assert.assertTrue(TWeekdays.isWeekday(new TDate(20140827)));
		Assert.assertTrue(TWeekdays.isWeekday(new TDate(20140828)));
		Assert.assertTrue(TWeekdays.isWeekday(new TDate(20140829)));
		Assert.assertFalse(TWeekdays.isWeekday(new TDate(20140830)));
		Assert.assertFalse(TWeekdays.isWeekday(new TDate(20140831)));
		
		// next
		Assert.assertEquals(new TDate(20140826), TWeekdays.next(new TDate(20140825)));
		Assert.assertEquals(new TDate(20140827), TWeekdays.next(new TDate(20140826)));
		Assert.assertEquals(new TDate(20140828), TWeekdays.next(new TDate(20140827)));
		Assert.assertEquals(new TDate(20140829), TWeekdays.next(new TDate(20140828)));
		Assert.assertEquals(new TDate(20140901), TWeekdays.next(new TDate(20140829)));
		
		// prev
		Assert.assertEquals(new TDate(20140825), TWeekdays.prev(new TDate(20140826)));
		Assert.assertEquals(new TDate(20140826), TWeekdays.prev(new TDate(20140827)));
		Assert.assertEquals(new TDate(20140827), TWeekdays.prev(new TDate(20140828)));
		Assert.assertEquals(new TDate(20140828), TWeekdays.prev(new TDate(20140829)));
		Assert.assertEquals(new TDate(20140829), TWeekdays.prev(new TDate(20140901)));
		
		// first
		Assert.assertEquals(new TDate(20140825), TWeekdays.first(new TDate(20140825)));
		Assert.assertEquals(new TDate(20140826), TWeekdays.first(new TDate(20140826)));
		Assert.assertEquals(new TDate(20140827), TWeekdays.first(new TDate(20140827)));
		Assert.assertEquals(new TDate(20140828), TWeekdays.first(new TDate(20140828)));
		Assert.assertEquals(new TDate(20140829), TWeekdays.first(new TDate(20140829)));
		Assert.assertEquals(new TDate(20140901), TWeekdays.first(new TDate(20140830)));
		Assert.assertEquals(new TDate(20140901), TWeekdays.first(new TDate(20140831)));
		
		// last
		Assert.assertEquals(new TDate(20140825), TWeekdays.last(new TDate(20140825)));
		Assert.assertEquals(new TDate(20140826), TWeekdays.last(new TDate(20140826)));
		Assert.assertEquals(new TDate(20140827), TWeekdays.last(new TDate(20140827)));
		Assert.assertEquals(new TDate(20140828), TWeekdays.last(new TDate(20140828)));
		Assert.assertEquals(new TDate(20140829), TWeekdays.last(new TDate(20140829)));
		Assert.assertEquals(new TDate(20140829), TWeekdays.last(new TDate(20140830)));
		Assert.assertEquals(new TDate(20140829), TWeekdays.last(new TDate(20140831)));
		
		// add +
		Assert.assertEquals(new TDate(20140825), TWeekdays.add(new TDate(20140825), 0));
		Assert.assertEquals(new TDate(20140826), TWeekdays.add(new TDate(20140825), 1));
		Assert.assertEquals(new TDate(20140827), TWeekdays.add(new TDate(20140825), 2));
		Assert.assertEquals(new TDate(20140828), TWeekdays.add(new TDate(20140825), 3));
		Assert.assertEquals(new TDate(20140829), TWeekdays.add(new TDate(20140825), 4));
		Assert.assertEquals(new TDate(20140901), TWeekdays.add(new TDate(20140825), 5));
		Assert.assertEquals(new TDate(20140902), TWeekdays.add(new TDate(20140825), 6));
		Assert.assertEquals(new TDate(20140903), TWeekdays.add(new TDate(20140825), 7));
		Assert.assertEquals(new TDate(20140904), TWeekdays.add(new TDate(20140825), 8));
		Assert.assertEquals(new TDate(20140905), TWeekdays.add(new TDate(20140825), 9));
		Assert.assertEquals(new TDate(20140908), TWeekdays.add(new TDate(20140825), 10));
		Assert.assertEquals(new TDate(20140909), TWeekdays.add(new TDate(20140825), 11));
		Assert.assertEquals(new TDate(20140910), TWeekdays.add(new TDate(20140825), 12));
		
		// add -
		Assert.assertEquals(new TDate(20140902), TWeekdays.add(new TDate(20140902), -0));
		Assert.assertEquals(new TDate(20140901), TWeekdays.add(new TDate(20140902), -1));
		Assert.assertEquals(new TDate(20140829), TWeekdays.add(new TDate(20140902), -2));
		Assert.assertEquals(new TDate(20140828), TWeekdays.add(new TDate(20140902), -3));
		Assert.assertEquals(new TDate(20140827), TWeekdays.add(new TDate(20140902), -4));
		Assert.assertEquals(new TDate(20140826), TWeekdays.add(new TDate(20140902), -5));
		Assert.assertEquals(new TDate(20140825), TWeekdays.add(new TDate(20140902), -6));
		Assert.assertEquals(new TDate(20140822), TWeekdays.add(new TDate(20140902), -7));
		Assert.assertEquals(new TDate(20140821), TWeekdays.add(new TDate(20140902), -8));
		Assert.assertEquals(new TDate(20140820), TWeekdays.add(new TDate(20140902), -9));
		Assert.assertEquals(new TDate(20140819), TWeekdays.add(new TDate(20140902), -10));
		Assert.assertEquals(new TDate(20140818), TWeekdays.add(new TDate(20140902), -11));
		Assert.assertEquals(new TDate(20140815), TWeekdays.add(new TDate(20140902), -12));
	}

}
