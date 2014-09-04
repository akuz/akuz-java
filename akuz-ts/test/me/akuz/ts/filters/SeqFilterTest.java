package me.akuz.ts.filters;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;

import me.akuz.ts.Seq;
import me.akuz.ts.SeqFilter;

public class SeqFilterTest {
	
	@Test
	public void testMultipleFiltersError() {
		
		Random rnd = ThreadLocalRandom.current();
		
		Seq<Integer> seq = new Seq<>();
		seq.add(0, rnd.nextInt(100));
		seq.add(1, rnd.nextInt(100));
		seq.add(2, rnd.nextInt(100));
		seq.add(3, rnd.nextInt(100));
		
		SeqFilter<Integer> seqFilter = new SeqFilter<>(seq.iterator());
		seqFilter.addFilter(new RepeatWithoutExpiry<Integer>());
		seqFilter.addFilter(new RepeatWithoutExpiry<Integer>());
		
		try {
			seqFilter.moveToTime(0);
			
			throw new IllegalStateException(
					"SeqFilter should have thrown an exception " +
					"because there are two filters producing values");
			
		} catch (Exception ex) {
			
			// all good
		}
	}
	
	@Test
	public void testWithCountExpiry() {
		
		Random rnd = ThreadLocalRandom.current();
		
		Seq<Integer> seq = new Seq<>();
		seq.add(0, rnd.nextInt(100));
		seq.add(1, rnd.nextInt(100));
		seq.add(5, rnd.nextInt(100));
		
		SeqFilter<Integer> seqFilter = new SeqFilter<>(seq.iterator());
		seqFilter.addFilter(new RepeatWithNumExpiry<Integer>(1));
		
		seqFilter.moveToTime(0);
		
		Assert.assertNotNull(seqFilter.getCurrItem());
		Assert.assertEquals(
				seq.getItems().get(0).getInteger(),
				seqFilter.getCurrItem().getInteger());
		
		seqFilter.moveToTime(1);
		
		Assert.assertNotNull(seqFilter.getCurrItem());
		Assert.assertEquals(
				seq.getItems().get(1).getInteger(),
				seqFilter.getCurrItem().getInteger());
		
		seqFilter.moveToTime(2);
		
		Assert.assertNotNull(seqFilter.getCurrItem());
		Assert.assertEquals(
				seq.getItems().get(1).getInteger(),
				seqFilter.getCurrItem().getInteger());
		
		seqFilter.moveToTime(3);
		
		Assert.assertNull(seqFilter.getCurrItem());
		
		seqFilter.moveToTime(5);
		
		Assert.assertNotNull(seqFilter.getCurrItem());
		Assert.assertEquals(
				seq.getItems().get(2).getInteger(),
				seqFilter.getCurrItem().getInteger());
		
		seqFilter.moveToTime(6);
		
		Assert.assertNotNull(seqFilter.getCurrItem());
		Assert.assertEquals(
				seq.getItems().get(2).getInteger(),
				seqFilter.getCurrItem().getInteger());
		
		seqFilter.moveToTime(7);
		
		Assert.assertNull(seqFilter.getCurrItem());
	}

}
