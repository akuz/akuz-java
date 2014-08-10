package me.akuz.ts;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;

public class SeqIterTest {
	
	@Test
	public void testSimple() {
		
		Random rnd = ThreadLocalRandom.current();
		
		Seq<Integer> seq = new Seq<>();
		seq.add(0, rnd.nextInt(100));
		seq.add(1, rnd.nextInt(100));
		seq.add(2, rnd.nextInt(100));
		seq.add(3, rnd.nextInt(100));
		seq.add(4, rnd.nextInt(100));
		
		SeqIterator<Integer> iter = new SeqIterator<>(seq);
		
		Assert.assertNull(iter.getCurrItem());
		Assert.assertEquals(0, iter.getMovedItems().size());
		
		iter.moveToTime(0);
		
		Assert.assertNotNull(iter.getCurrItem());
		Assert.assertEquals(seq.getItems().get(0).getInteger(), iter.getCurrItem().getInteger());
		Assert.assertEquals(1, iter.getMovedItems().size());
		
		iter.moveToTime(2);
		
		Assert.assertNotNull(iter.getCurrItem());
		Assert.assertEquals(seq.getItems().get(2).getInteger(), iter.getCurrItem().getInteger());
		Assert.assertEquals(2, iter.getMovedItems().size());
		
		iter.moveToTime(10);
		
		Assert.assertNull(iter.getCurrItem());
		Assert.assertEquals(2, iter.getMovedItems().size());
	}

}
