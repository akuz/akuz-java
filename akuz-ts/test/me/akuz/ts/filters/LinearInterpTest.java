package me.akuz.ts.filters;

import java.util.Date;

import me.akuz.ts.Seq;
import me.akuz.ts.SeqFilter;
import me.akuz.ts.filters.interp.LinearInterpDateAhead;

import org.junit.Assert;
import org.junit.Test;

public class LinearInterpTest {
	
	@Test
	public void testLinearInterpDateAhead() {
		
		Seq<Date> seq = new Seq<>();
		seq.add(new Date(1000), 0);
		seq.add(new Date(2000), 1.0);
		seq.add(new Date(4000), -1);
		
		SeqFilter<Date> filter = new SeqFilter<>(seq);
		filter.addFilter(new LinearInterpDateAhead());
		
		Assert.assertNull(filter.getCurrItem());
		
		filter.moveToTime(new Date(1000));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(0.0 - filter.getCurrItem().getDouble()) < 0.00001);
		
		filter.moveToTime(new Date(1333));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(0.333 - filter.getCurrItem().getDouble()) < 0.00001);
		
		filter.moveToTime(new Date(1500));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(0.5 - filter.getCurrItem().getDouble()) < 0.00001);
		
		filter.moveToTime(new Date(2000));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(1.0 - filter.getCurrItem().getDouble()) < 0.00001);
		
		filter.moveToTime(new Date(2500));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(0.5 - filter.getCurrItem().getDouble()) < 0.00001);
		
		filter.moveToTime(new Date(3000));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(0.0 - filter.getCurrItem().getDouble()) < 0.00001);
		
		filter.moveToTime(new Date(3500));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(-0.5 - filter.getCurrItem().getDouble()) < 0.00001);
		
		filter.moveToTime(new Date(4000));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(-1.0 - filter.getCurrItem().getDouble()) < 0.00001);
		
		filter.moveToTime(new Date(5000));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(-1.0 - filter.getCurrItem().getDouble()) < 0.00001);
		
	}

}
