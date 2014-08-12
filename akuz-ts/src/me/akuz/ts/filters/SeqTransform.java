package me.akuz.ts.filters;

import me.akuz.core.Out;
import me.akuz.ts.Seq;
import me.akuz.ts.TItem;
import me.akuz.ts.sync.Synchronizable;

public final class SeqTransform<T extends Comparable<T>>
implements Synchronizable<T> {

	private final SeqFilter<T> _seqFilter;
	private final Seq<T> _seqOutput;
	
	public SeqTransform(final SeqFilter<T> seqFilter) {
		_seqFilter = seqFilter;
		_seqOutput = new Seq<>();
	}
	
	public SeqFilter<T> getFilter() {
		return _seqFilter;
	}
	
	public Seq<T> getOutput() {
		return _seqOutput;
	}
	
	@Override
	public T getCurrTime() {
		return _seqFilter.getCurrTime();
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {
		return _seqFilter.getNextTime(nextTime);
	}
	
	@Override
	public void moveToTime(T time) {
		
		_seqFilter.moveToTime(time);
		TItem<T> filteredItem = _seqFilter.getCurrItem();
		
		if (filteredItem != null) {
			_seqOutput.add(filteredItem);
		}
	}

}
