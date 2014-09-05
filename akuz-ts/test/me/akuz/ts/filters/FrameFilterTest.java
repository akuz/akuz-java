package me.akuz.ts.filters;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.ts.Frame;
import me.akuz.ts.FrameFilter;

import org.junit.Assert;
import org.junit.Test;

public class FrameFilterTest {

	@Test
	public void testNoFilters() {

		Frame<String, Integer> frame = new Frame<>();
		FrameFilter<String, Integer> filter = new FrameFilter<>(frame.iterator());
		
		try {
			filter.moveToTime(0);
			
			throw new IllegalStateException(
					"FrameFilter should have thrown exception " +
					"because there are not sequence filters");

		} catch (Exception ex) {
			
			// all good
		}
	}

	@Test
	public void testMultipleSeqs() {
		
		Random rnd = ThreadLocalRandom.current();
		
		Frame<String, Integer> frame = new Frame<>();
		
		frame.add("f1", 0, rnd.nextInt(100));
		frame.add("f2", 0, rnd.nextInt(100));
		frame.add("f3", 0, rnd.nextInt(100));
		
		frame.add("f1", 1, rnd.nextInt(100));
		frame.add("f2", 1, rnd.nextInt(100));
		
		frame.add("f1", 2, rnd.nextInt(100));
		
		FrameFilter<String, Integer> filter = new FrameFilter<>(frame.iterator());
		filter.addFilter("f1", new RepeatValueWithNumExpiry<Integer>(1));
		filter.addFilter("f3", new RepeatValueWithNumExpiry<Integer>(1));
		
		filter.moveToTime(0);
		
		Assert.assertEquals(
				frame.getSeq("f1").getItems().get(0).getInteger(),
				filter.getCurrItem("f1").getInteger());
		
		Assert.assertNull(
				filter.getCurrItem("f2"));
		
		Assert.assertEquals(
				frame.getSeq("f3").getItems().get(0).getInteger(),
				filter.getCurrItem("f3").getInteger());
		
		filter.moveToTime(1);
		
		Assert.assertEquals(
				frame.getSeq("f1").getItems().get(1).getInteger(),
				filter.getCurrItem("f1").getInteger());
		
		Assert.assertNull(
				filter.getCurrItem("f2"));
		
		Assert.assertEquals(
				frame.getSeq("f3").getItems().get(0).getInteger(),
				filter.getCurrItem("f3").getInteger());
		
		filter.moveToTime(2);
		
		Assert.assertEquals(
				frame.getSeq("f1").getItems().get(2).getInteger(),
				filter.getCurrItem("f1").getInteger());
		
		Assert.assertNull(
				filter.getCurrItem("f2"));
		
		Assert.assertNull(
				filter.getCurrItem("f3"));
		
		filter.moveToTime(4);
		
		Assert.assertNull(
				filter.getCurrItem("f1"));
		
		Assert.assertNull(
				filter.getCurrItem("f2"));
		
		Assert.assertNull(
				filter.getCurrItem("f3"));
		
	}
}
