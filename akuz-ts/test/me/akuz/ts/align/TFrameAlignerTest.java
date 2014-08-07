package me.akuz.ts.align;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.TFrame;
import me.akuz.ts.filters.TFrameStepper;

import org.junit.Assert;
import org.junit.Test;

public final class TFrameAlignerTest {
	
	private static TFrame<String, Integer> createSimpleFrame() {
		
		TFrame<String, Integer> frame = new TFrame<>();
		
		frame.add("f1", 0, 10);
		frame.add("f1", 1, 11);
		frame.add("f1", 2, 12);
		frame.add("f1", 3, 13);
		
		frame.add("f2", 0, 20);
		frame.add("f2", 3, 23);
		
		return frame;
	}

	@Test
	public void testSimpleValues() {
		
		TFrame<String, Integer> frame = createSimpleFrame();
		
		List<Integer> times = new ArrayList<>();
		times.add(0);
		times.add(2);
		times.add(3);
		
		TFrameStepper<String, Integer> iter = new TFrameStepper<>(frame, frame.getKeys(), times);
		
		Assert.assertTrue(iter.hasNext());
		iter.next();

		Assert.assertEquals((Integer)0, iter.getCurrTime());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)0, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)10, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f2").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f2"));
		Assert.assertEquals((Integer)0, iter.getCurrItems().get("f2").getTime());
		Assert.assertEquals((Integer)20, iter.getCurrItems().get("f2").getInteger());
		
		Assert.assertTrue(iter.hasNext());
		iter.next();

		Assert.assertEquals((Integer)2, iter.getCurrTime());
		
		Assert.assertEquals(2, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)2, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)12, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(0, iter.getMovedItems().get("f2").size());
		Assert.assertFalse(iter.getCurrItems().containsKey("f2"));
		
		Assert.assertTrue(iter.hasNext());
		iter.next();

		Assert.assertEquals((Integer)3, iter.getCurrTime());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)3, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)13, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f2").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f2"));
		Assert.assertEquals((Integer)3, iter.getCurrItems().get("f2").getTime());
		Assert.assertEquals((Integer)23, iter.getCurrItems().get("f2").getInteger());
		
		Assert.assertFalse(iter.hasNext());
		
	}

	@Test
	public void testSimpleBadTimes() {
		
		TFrame<String, Integer> frame = createSimpleFrame();
		
		List<Integer> times = new ArrayList<>();
		times.add(0);
		times.add(2);
		times.add(1);
		
		TFrameStepper<String, Integer> iter = new TFrameStepper<>(frame, frame.getKeys(), times);
		
		try {
			while (iter.hasNext()) {
				iter.next();
			}
			throw new IllegalStateException("Should have thrown exception because of bad times");
		} catch (Exception ex) {
			// expected exception
			return;
		}
	}

}
