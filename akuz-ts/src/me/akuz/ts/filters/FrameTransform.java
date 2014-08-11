package me.akuz.ts.filters;

import java.util.List;

import me.akuz.ts.Frame;
import me.akuz.ts.TItem;
import me.akuz.ts.sync.Synchronizable;

public final class FrameTransform<K, T extends Comparable<T>> 
implements Synchronizable<T> {
	
	private final FrameFilter<K, T> _frameFilter;
	private final List<K> _frameFilterKeys;
	private final Frame<K, T> _frameOutput;
	
	public FrameTransform(final FrameFilter<K, T> frameFilter) {
		_frameFilter = frameFilter;
		_frameFilterKeys = frameFilter.getKeys();
		_frameOutput = new Frame<>();
	}
	
	public FrameFilter<K, T> getFilter() {
		return _frameFilter;
	}
	
	public Frame<K, T> getOutput() {
		return _frameOutput;
	}

	@Override
	public void moveToTime(T time) {
		
		_frameFilter.moveToTime(time);
		
		for (int i=0; i<_frameFilterKeys.size(); i++) {
			
			final K key = _frameFilterKeys.get(i);
			final TItem<T> currItem = _frameFilter.getCurrItem(key);
			if (currItem != null) {
				_frameOutput.add(key, currItem);
			}
		}
	}

}
