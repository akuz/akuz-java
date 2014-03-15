package me.akuz.qf.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Not entirely efficient time correlator.
 *
 */
public final class TimeCorrelator {
	
	private final Date _minDate;
	private final Date _maxDate;
	private final Map<String, Integer> _tickerIndices;
	private final List<List<DateVolumeClose>> _lists;
	private final List<Integer> _listCursors;
	private final Map<Integer, Integer> _currentIndexCursors;
	
	public TimeCorrelator(Map<String, List<DateVolumeClose>> map, Date minDate, Date maxDate) {
		
		_minDate = minDate;
		_maxDate = maxDate;
		_tickerIndices = new HashMap<>();
		_lists = new ArrayList<>();
		_listCursors = new ArrayList<>();
		for (Entry<String, List<DateVolumeClose>> entry : map.entrySet()) {
			
			String ticker = entry.getKey();
			List<DateVolumeClose> list = entry.getValue();
			
			// add ticker and corresponding list
			_tickerIndices.put(ticker, _lists.size());
			_lists.add(list);
			
			// set list start cursor
			int startCursor = list.size();
			for (int cursor=0; cursor<list.size(); cursor++) {
				DateVolumeClose dvc = list.get(cursor);
				if (dvc.getVolume().equals(0)) {
					continue;
				}
				if (_minDate == null || _minDate.compareTo(dvc.getDate()) <= 0) {
					startCursor = cursor;
					break;
				}
			}
			_listCursors.add(startCursor);
		}
		_currentIndexCursors = new HashMap<>();
	}
	
	public boolean next() {
		
		// reset current
		Date nextDate = null;
		_currentIndexCursors.clear();
		
		for (int index=0; index<_listCursors.size(); index++) {

			Integer cursor = _listCursors.get(index);
			List<DateVolumeClose> list = _lists.get(index);
			
			// check end of list
			if (cursor >= list.size()) {
				continue;
			}
			
			// check max date not reached
			DateVolumeClose dvc = list.get(cursor);
			if (_maxDate != null && _maxDate.compareTo(dvc.getDate()) < 0) {
				_listCursors.set(index, list.size());
				continue;
			}
			
			// update next date
			if (nextDate == null) {
				nextDate = dvc.getDate();
				_currentIndexCursors.put(index, cursor);
			} else if (nextDate.equals(dvc.getDate())) {
				_currentIndexCursors.put(index, cursor);
			} else if (nextDate.compareTo(dvc.getDate()) > 0) {
				nextDate = dvc.getDate();
				_currentIndexCursors.clear();
				_currentIndexCursors.put(index, cursor);
			}
		}
		
		// shift list cursors to next for the *current* lists
		for (Entry<Integer, Integer> entry : _currentIndexCursors.entrySet()) {
			Integer index = entry.getKey();
			Integer cursor = entry.getValue();
			List<DateVolumeClose> list = _lists.get(index);
			for (cursor = cursor+1; cursor<list.size(); cursor++) {
				if (cursor >= list.size()) {
					break;
				}
				DateVolumeClose dvc = list.get(cursor);
				if (dvc.getVolume() > 0) {
					break;
				}
			}
			_listCursors.set(entry.getKey(), cursor);
		}
		
		return _currentIndexCursors.size() > 0;
	}

	public int getCurrentCount() {
		return _currentIndexCursors.size();
	}
	
	public DateVolumeClose getCurrent(String ticker) {
		
		Integer index = _tickerIndices.get(ticker);
		if (index == null) {
			return null;
		}
		Integer cursor = _currentIndexCursors.get(index);
		if (cursor == null) {
			return null;
		}
		return _lists.get(index).get(cursor);
	}
}
