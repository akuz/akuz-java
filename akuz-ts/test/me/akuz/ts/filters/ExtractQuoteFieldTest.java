package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.TDateTime;
import me.akuz.ts.Quote;
import me.akuz.ts.QuoteField;
import me.akuz.ts.Seq;
import me.akuz.ts.SeqFilter;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class ExtractQuoteFieldTest {

	@Test
	public void testSimple() {
		
		Seq<TDateTime> seq = new Seq<>();
		seq.add(new TDateTime(new DateTime(2013, 1, 2, 9, 0)), Quote.build().set(QuoteField.AdjOpen, 1.0).create());
		seq.add(new TDateTime(new DateTime(2013, 1, 2, 17, 0)), Quote.build().set(QuoteField.AdjClose, 2.0).create());
		seq.add(new TDateTime(new DateTime(2013, 1, 3, 9, 0)), Quote.build().set(QuoteField.AdjOpen, 1.0).create());
		seq.add(new TDateTime(new DateTime(2013, 1, 3, 17, 0)), Quote.build().set(QuoteField.AdjClose, 1.5).create());
		
		SeqFilter<TDateTime> filter = new SeqFilter<>(seq.iterator());
		List<QuoteField> quoteFields = new ArrayList<>(2);
		quoteFields.add(QuoteField.AdjClose);
		quoteFields.add(QuoteField.AdjOpen);
		filter.addFilter(new ExtractQuoteField<TDateTime>(quoteFields));
		
		filter.moveToTime(new TDateTime(new DateTime(2013, 1, 2, 7, 59)));
		Assert.assertNull(filter.getCurrItem());
		Assert.assertTrue(filter.getMovedItems() == null || filter.getMovedItems().size() == 0);
		
		filter.moveToTime(new TDateTime(new DateTime(2013, 1, 2, 9, 0)));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(filter.getCurrItem().getDouble() - 1.0) < 0.0000001);
		Assert.assertNotNull(filter.getMovedItems());
		Assert.assertEquals(1, filter.getMovedItems().size());
		Assert.assertTrue(Math.abs(filter.getMovedItems().get(0).getDouble() - 1.0) < 0.0000001);
		
		filter.moveToTime(new TDateTime(new DateTime(2013, 1, 2, 18, 0)));
		Assert.assertNull(filter.getCurrItem());
		Assert.assertNotNull(filter.getMovedItems());
		Assert.assertEquals(1, filter.getMovedItems().size());
		Assert.assertTrue(Math.abs(filter.getMovedItems().get(0).getDouble() - 2.0) < 0.0000001);
		
		filter.moveToTime(new TDateTime(new DateTime(2013, 1, 3, 17, 0)));
		Assert.assertNotNull(filter.getCurrItem());
		Assert.assertTrue(Math.abs(filter.getCurrItem().getDouble() - 1.5) < 0.0000001);
		Assert.assertNotNull(filter.getMovedItems());
		Assert.assertEquals(2, filter.getMovedItems().size());
		Assert.assertTrue(Math.abs(filter.getMovedItems().get(0).getDouble() - 1.0) < 0.0000001);
		Assert.assertTrue(Math.abs(filter.getMovedItems().get(1).getDouble() - 1.5) < 0.0000001);

	}
}
