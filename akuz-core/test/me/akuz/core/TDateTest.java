package me.akuz.core;

import org.junit.Assert;
import org.junit.Test;

public class TDateTest {
	
	@Test
	public void testSimple() {
		
		try {
			new TDate(2014, 8, 33);
			Assert.fail("Invalid date accepted");
		} catch (Exception ex) {
			// expected
		}
		try {
			new TDate(2014, 0, 12);
			Assert.fail("Invalid date accepted");
		} catch (Exception ex) {
			// expected
		}
		try {
			new TDate(2014, -8, 12);
			Assert.fail("Invalid date accepted");
		} catch (Exception ex) {
			// expected
		}
		try {
			new TDate(-2);
			Assert.fail("Invalid date accepted");
		} catch (Exception ex) {
			// expected
		}
		{
			TDate date = new TDate(101);
			Assert.assertEquals(0, date.getYear());
			Assert.assertEquals(1, date.getMonthOfYear());
			Assert.assertEquals(1, date.getDayOfMonth());
		}
		{
			TDate date = new TDate("0000-01-01");
			Assert.assertEquals(0, date.getYear());
			Assert.assertEquals(1, date.getMonthOfYear());
			Assert.assertEquals(1, date.getDayOfMonth());
		}
		{
			TDate date = new TDate(0, 1, 1);
			Assert.assertEquals(0, date.getYear());
			Assert.assertEquals(1, date.getMonthOfYear());
			Assert.assertEquals(1, date.getDayOfMonth());
		}
		{
			TDate date = new TDate(2014, 8, 17);
			Assert.assertEquals(2014, date.getYear());
			Assert.assertEquals(8, date.getMonthOfYear());
			Assert.assertEquals(17, date.getDayOfMonth());
		}
		{
			TDate date = new TDate(20140817);
			Assert.assertEquals(2014, date.getYear());
			Assert.assertEquals(8, date.getMonthOfYear());
			Assert.assertEquals(17, date.getDayOfMonth());
		}
		{
			TDate date = new TDate("2014-08-17");
			Assert.assertEquals(2014, date.getYear());
			Assert.assertEquals(8, date.getMonthOfYear());
			Assert.assertEquals(17, date.getDayOfMonth());
		}
		{
			TDate date = new TDate(20140801);
			Assert.assertEquals(2014, date.getYear());
			Assert.assertEquals(8, date.getMonthOfYear());
			Assert.assertEquals(1, date.getDayOfMonth());
		}
		{
			TDate date = new TDate("2014-08-01");
			Assert.assertEquals(2014, date.getYear());
			Assert.assertEquals(8, date.getMonthOfYear());
			Assert.assertEquals(1, date.getDayOfMonth());
		}
		{
			TDate date = new TDate(20140131);
			Assert.assertEquals(2014, date.getYear());
			Assert.assertEquals(1, date.getMonthOfYear());
			Assert.assertEquals(31, date.getDayOfMonth());
		}
		{
			TDate date = new TDate("2014-01-31");
			Assert.assertEquals(2014, date.getYear());
			Assert.assertEquals(1, date.getMonthOfYear());
			Assert.assertEquals(31, date.getDayOfMonth());
		}
	}

}
