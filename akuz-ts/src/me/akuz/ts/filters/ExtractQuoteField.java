package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.Quote;
import me.akuz.ts.QuoteField;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class ExtractQuoteField<T extends Comparable<T>> extends Filter<T> {
	
	private final List<QuoteField> _quoteFields;
	
	/**
	 * Extract a specific quote field.
	 * 
	 */
	public ExtractQuoteField(final QuoteField quoteField) {
		if (quoteField == null) {
			throw new IllegalArgumentException("Quote field cannot be null");
		}
		_quoteFields = new ArrayList<>(1);
		_quoteFields.add(quoteField);
	}
	
	/**
	 * Extract quote fields by priority (try first, then next, etc).
	 * 
	 */
	public ExtractQuoteField(final List<QuoteField> quoteFields) {
		if (quoteFields == null) {
			throw new IllegalArgumentException("Quote fields cannot be null");
		}
		if (quoteFields.size() == 0) {
			throw new IllegalArgumentException("Quote fields cannot be empty");
		}
		_quoteFields = quoteFields;
	}

	@Override
	public void next(
			final T time, 
			final SeqCursor<T> cursor, 
			final TLog<T> log) {
		
		CurrTime.checkNew(_currTime, time);
		
		_currItem = null;
		_movedItems.clear();
		final List<TItem<T>> movedItems = cursor.getMovedItems();
		for (int i=0; i<movedItems.size(); i++) {
			
			final TItem<T> item = movedItems.get(i);
			final int cmp = time.compareTo(item.getTime());
			final Quote quote = item.get();
			
			for (int f=0; f<_quoteFields.size(); f++) {
				final QuoteField quoteField = _quoteFields.get(f);
				if (quote.has(quoteField)) {
					
					final double value = quote.getDouble(quoteField);
					final TItem<T> item2 = new TItem<>(item.getTime(), value);
	
					if (cmp == 0) {
						_currItem = item2;
					}
					_movedItems.add(item2);
					break;
				}
			}
		}
		
		_currTime = time;
	}

}
