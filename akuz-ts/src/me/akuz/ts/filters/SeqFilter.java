package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.akuz.core.Out;
import me.akuz.ts.Seq;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;
import me.akuz.ts.sync.Synchronizable;

/**
 * {@link Seq} filter, which applies one or more 
 * 1D filters to the underlying sequence and allows
 * to obtain current filtered item for the sequence.
 * 
 * Only one of the filters is allowed to generate the 
 * current item, otherwise an exception will occur.
 *
 */
public final class SeqFilter<T extends Comparable<T>> 
implements Synchronizable<T>, SeqCursor<T> {
	
	private String _fieldName;
	private final SeqIterator<T> _seqIter;
	private final List<Filter<T>> _filters;
	private TItem<T> _currFilteredItem;
	private T _currTime;
	private TLog _log;
	
	public SeqFilter(final Seq<T> seq) {
		if (seq == null) {
			throw new IllegalArgumentException("Cannot filter null sequence");
		}
		_seqIter = new SeqIterator<>(seq);
		_filters = new ArrayList<>();
	}
	
	public SeqFilter(final Seq<T> seq, final Filter<T> filter) {
		this(seq);
		addFilter(filter);
	}
	
	public SeqFilter(final Seq<T> seq, final Collection<Filter<T>> filters) {
		this(seq);
		addFilters(filters);
	}
	
	public String getFieldName() {
		return _fieldName != null ? _fieldName : "unspecified";
	}
	
	public void setFieldName(final String fieldName) {
		_fieldName = fieldName;
	}
	
	public void setLog(final TLog log) {
		_log = log;
	}
	
	public void addFilter(Filter<T> filter) {
		final Filter<T> filterCopy = filter.clone();
		if (_fieldName != null) {
			filterCopy.setFieldName(_fieldName);
		}
		_filters.add(filterCopy);
	}
	
	public void addFilters(Collection<Filter<T>> filters) {
		for (final Filter<T> filter : filters) {
			addFilter(filter);
		}
	}
	
	@Override
	public T getCurrTime() {
		return _currTime;
	}
	
	@Override
	public TItem<T> getCurrItem() {
		return _currFilteredItem;
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {
		return _seqIter.getNextTime(nextTime);
	}

	@Override
	public void moveToTime(T time) {
		
		if (_filters.size() == 0) {
			throw new IllegalStateException(
					"SeqFilter on field \"" + getFieldName() + 
					"\" does not have any 1D filters assigned");
		}

		if (_currTime != null) {
			final int cmp = _currTime.compareTo(time);
			if (cmp > 0)
				throw new IllegalStateException(
						"Trying to move backwards in time from " + 
						_currTime + " to " + time);
			if (cmp == 0)
				return;
		}
		
		_seqIter.moveToTime(time);
		
		TItem<T> newCurrFilteredItem = null;
		
		for (int i=0; i<_filters.size(); i++) {
			
			final Filter<T> filter = _filters.get(i);
			
			filter.next(
					_log,
					time,
					_seqIter.getCurrItem(),
					_seqIter.getMovedItems());
			
			final TItem<T> proposedItem = filter.getCurrItem();
			
			if (proposedItem != null) {
				if (newCurrFilteredItem != null) {
					throw new IllegalStateException(
							"Two 1D filters have proposed current item " +
							"for SeqFilter on field \"" + getFieldName() +
							"\", cannot choose between them");
				}
				newCurrFilteredItem = proposedItem;
			}
		}
		_currFilteredItem = newCurrFilteredItem;
		_currTime = time;
	}

}
