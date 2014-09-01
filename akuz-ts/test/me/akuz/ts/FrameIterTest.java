package me.akuz.ts;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;

public class FrameIterTest {
	
	@Test
	public void testSimple() {
		
		Random rnd = ThreadLocalRandom.current();
		
		Frame<String, Integer> frame = new Frame<>();
		
		frame.add("f1", 0, rnd.nextInt(100));
		frame.add("f2", 0, rnd.nextInt(100));

		frame.add("f1", 1, rnd.nextInt(100));
		
		frame.add("f1", 2, rnd.nextInt(100));
		frame.add("f2", 2, rnd.nextInt(100));

		frame.add("f1", 3, rnd.nextInt(100));
		frame.add("f2", 3, rnd.nextInt(100));

		frame.add("f1", 4, rnd.nextInt(100));
		
		FrameIterator<String, Integer> iter = new FrameIterator<>(frame);
		
		try {
			Assert.assertNull(iter.getCurrTime());
			Assert.fail();
		} catch (Exception ex) {
			// expected
		}
		
		try {
			Assert.assertEquals(0, iter.getCurrItems().size());
			Assert.fail();
		} catch (Exception ex) {
			// expected
		}

		iter.moveToTime(0);
		
		Assert.assertNotNull(iter.getCurrTime());
		Assert.assertEquals((Integer)0, iter.getCurrTime());
		
		Assert.assertEquals(
				frame.getSeq("f1").getItems().get(0).getInteger(), 
				iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(
				1, iter.getMovedItems().get("f1").size());
		
		Assert.assertEquals(
				frame.getSeq("f2").getItems().get(0).getInteger(), 
				iter.getCurrItems().get("f2").getInteger());
		
		Assert.assertEquals(
				1, iter.getMovedItems().get("f2").size());

		iter.moveToTime(2);
		
		Assert.assertNotNull(iter.getCurrTime());
		Assert.assertEquals((Integer)2, iter.getCurrTime());
		
		Assert.assertEquals(
				frame.getSeq("f1").getItems().get(2).getInteger(), 
				iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(
				2, iter.getMovedItems().get("f1").size());
		
		Assert.assertEquals(
				frame.getSeq("f2").getItems().get(1).getInteger(), 
				iter.getCurrItems().get("f2").getInteger());
		
		Assert.assertEquals(
				1, iter.getMovedItems().get("f2").size());

		iter.moveToTime(10);
		
		Assert.assertNotNull(iter.getCurrTime());
		Assert.assertEquals((Integer)10, iter.getCurrTime());
		
		Assert.assertNull(
				iter.getCurrItems().get("f1"));
		
		Assert.assertEquals(
				2, iter.getMovedItems().get("f1").size());
		
		Assert.assertNull( 
				iter.getCurrItems().get("f2"));
		
		Assert.assertEquals(
				1, iter.getMovedItems().get("f2").size());
		
	}

}
