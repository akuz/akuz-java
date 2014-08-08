package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.ts.FrameIter;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;
import me.akuz.ts.sync.Synchronizable;

public final class FrameFilter<K, T extends Comparable<T>>
implements FrameIter<K, T>, Synchronizable<T> {
	
	/**
	 * Frame filter builder class.
	 * 
	 */
	public static final class Builder<K, T extends Comparable<T>> {
		
		private final FrameFilter<K, T> _proto;
		
		private Builder(final FrameWalker<K, T> frameWalker, final List<K> keys) {
			_proto = new FrameFilter<>(frameWalker, keys);
		}

		public Builder<K, T> addAllKeysFilters(final Collection<Filter<T>> filters) {
			for (final Filter<T> filter : filters) {
				addAllKeysFilter(filter);
			}
			return this;
		}

		public Builder<K, T> addAllKeysFilter(final Filter<T> filter) {
			final List<K> keys = _proto._frameWalker.getKeys();
			for (int i=0; i<keys.size(); i++) {
				final K key = keys.get(i);
				addKeyFilter(key, filter);
			}
			return this;
		}
		
		public Builder<K, T> addKeyFilters(final K key, final List<Filter<T>> filters) {
			for (final Filter<T> filter : filters) {
				addKeyFilter(key, filter);
			}
			return this;
		}
			
		public Builder<K, T> addKeyFilter(final K key, final Filter<T> filter) {
			
			if (_proto._underlyingKeysIndex.getIndex(key) == null) {
				throw new IllegalArgumentException(
						"Cannot add a filter for key \"" +
						key + "\" because this key is not " +
						"present in the underlying data");
			}
			_proto._filterKeysIndex.ensure(key);
			
			List<Filter<T>> keyFilters = _proto._keyFilters.get(key);
			if (keyFilters == null) {
				keyFilters = new ArrayList<>();
				_proto._keyFilters.put(key, keyFilters);
			}
			final Filter<T> filterCopy = filter.clone();
			filterCopy.setFieldName(key.toString());
			keyFilters.add(filterCopy);
			return this;
		}
		
		public Builder<K, T> setLog(final TLog log) {
			_proto._log = log;
			return this;
		}
		
		public FrameFilter<K, T> build() {
			return _proto;
		}
	}
	
	/**
	 * Create filter on all keys in frame walker.
	 * Don't forget to add filters for all keys!
	 */
	public static <K, T extends Comparable<T>> 
	Builder<K, T> onAllKeysOf(final FrameWalker<K, T> frameWalker) {
		
		return new Builder<>(frameWalker, frameWalker.getKeys());
	}
	
	/**
	 * Create filter on some keys in frame walker.
	 * Don't forget to add filters for some keys!
	 */
	public static <K, T extends Comparable<T>> 
	Builder<K, T> onSomeKeysOf(final FrameWalker<K, T> frameWalker) {
		
		return new Builder<>(frameWalker, new ArrayList<K>());
	}
	
	/**
	 * Private frame filter data.
	 * 
	 */
	private final FrameWalker<K, T> _frameWalker;
	private final Index<K> _underlyingKeysIndex;
	private final Index<K> _filterKeysIndex;
	private final Map<K, List<Filter<T>>> _keyFilters;
	private final Map<K, TItem<T>> _currStateItems;
	private TLog _log;
	
	private FrameFilter(final FrameWalker<K, T> frameWalker, List<K> keys) {
		
		if (frameWalker == null) {
			throw new IllegalArgumentException("Walker cannot be null");
		}
		_frameWalker = frameWalker;
		_underlyingKeysIndex = new HashIndex<>();
		_underlyingKeysIndex.ensureAll(frameWalker.getKeys());
		_filterKeysIndex = new HashIndex<>();
		_filterKeysIndex.ensureAll(keys);
		_keyFilters = new HashMap<>();
		_currStateItems = new HashMap<>();
	}
	
	@Override
	public T getCurrTime() {
		return _frameWalker.getCurrTime();
	}
	
	@Override
	public Map<K, TItem<T>> getCurrItems() {
		return _currStateItems;
	}
	
	@Override
	public boolean hasNext() {
		return _frameWalker.hasNext();
	}
	
	@Override
	public void next() {
		
		_frameWalker.next();
		final T currTime = _frameWalker.getCurrTime();
		final Map<K, TItem<T>> currItemsMap = _frameWalker.getCurrItems();
		final Map<K, List<TItem<T>>> movedItemsMap = _frameWalker.getMovedItems();
		
		_currStateItems.clear();
		for (int i=0; i<_filterKeysIndex.size(); i++) {
			
			final K key = _filterKeysIndex.getValue(i);
			final TItem<T> currItem = currItemsMap.get(key);
			final List<TItem<T>> movedItems = movedItemsMap.get(key);
			
			final List<Filter<T>> filters = _keyFilters.get(key);
			
			TItem<T> currStateItem = null;
			
			if (filters == null) {
				// FIXME: throw if not filters?
				currStateItem = currItem;
			} else {
				for (int j=0; j<filters.size(); j++) {
					
					final Filter<T> state = filters.get(j);
					
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
			}
			_currStateItems.put(key, currStateItem);
		}
	}

	@Override
	public void moveToTime(T time) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
