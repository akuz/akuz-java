package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

public final class TFrameFilter<K, T extends Comparable<T>> {
	
	/**
	 * Frame roll builder class.
	 * 
	 */
	public static final class Builder<K, T extends Comparable<T>> {
		
		private final TFrameFilter<K, T> _proto;
		
		public Builder(TFrameAligner<K, T> frameIterator) {
			_proto = new TFrameFilter<>(frameIterator);
		}
		
		public Builder<K, T> addAllKeysFilter(final TFilter<T> filterProto) {
			final List<K> keys = _proto._frameAligner.getKeys();
			for (int i=0; i<keys.size(); i++) {
				final K key = keys.get(i);
				addKeyFilter(key, filterProto);
			}
			return this;
		}
		
		public Builder<K, T> addKeyFilter(final K key, final TFilter<T> filterProto) {
			List<TFilter<T>> filters = _proto._filters.get(key);
			if (filters == null) {
				filters = new ArrayList<>();
				_proto._filters.put(key, filters);
			}
			final TFilter<T> filter = filterProto.clone();
			filter.setFieldName(key.toString());
			filters.add(filter);
			return this;
		}
		
		public Builder<K, T> setLog(final TLog log) {
			_proto._log = log;
			return this;
		}
		
		public TFrameFilter<K, T> build() {
			return _proto;
		}
	}
	
	/**
	 * Create frame roll builder.
	 * 
	 */
	public static <K, T extends Comparable<T>> 
	Builder<K, T> on(final TFrameAligner<K, T> frameAligner) {
		
		return new Builder<>(frameAligner);
	}
	
	/**
	 * Private frame roll data.
	 * 
	 */
	private final TFrameAligner<K, T> _frameAligner;
	private final Map<K, List<TFilter<T>>> _filters;
	private final Map<K, TItem<T>> _currStateItems;
	private TLog _log;
	
	private TFrameFilter(final TFrameAligner<K, T> frameAligner) {
		
		if (frameAligner == null) {
			throw new IllegalArgumentException("Iterator cannot be null");
		}
		_frameAligner = frameAligner;
		_filters = new HashMap<>();
		_currStateItems = new HashMap<>();
	}
	
	public T getCurrTime() {
		return _frameAligner.getCurrTime();
	}
	
	public Map<K, TItem<T>> getCurrItems() {
		return _currStateItems;
	}
	
	public boolean hasNext() {
		return _frameAligner.hasNext();
	}
	
	public void next() {
		
		_frameAligner.next();
		final T currTime = _frameAligner.getCurrTime();
		final Map<K, TItem<T>> currItemsMap = _frameAligner.getCurrItems();
		final Map<K, List<TItem<T>>> movedItemsMap = _frameAligner.getMovedItems();
		
		_currStateItems.clear();
		final List<K> keys = _frameAligner.getKeys();
		for (int i=0; i<keys.size(); i++) {
			
			final K key = keys.get(i);
			final TItem<T> currItem = currItemsMap.get(key);
			final List<TItem<T>> movedItems = movedItemsMap.get(key);
			
			final List<TFilter<T>> states = _filters.get(key);
			
			TItem<T> currStateItem = null;
			for (int j=0; j<states.size(); j++) {
				
				final TFilter<T> state = states.get(j);
				
				state.next(_log, currTime, currItem, movedItems);
				
				final TItem<T> proposedStateItem = state.getCurrent();
				if (proposedStateItem != null) {
					if (currStateItem != null) {
						throw new IllegalStateException(
								"Two current states generated for key " +
								key + " at time " + currTime + ", 1: " +
								currStateItem + ", 2: " + proposedStateItem);
					}
					currStateItem = proposedStateItem;
				}
			}
			_currStateItems.put(key, currStateItem);
		}
	}
	
	
	
}
