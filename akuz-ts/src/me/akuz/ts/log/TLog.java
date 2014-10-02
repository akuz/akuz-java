package me.akuz.ts.log;

import java.util.ArrayList;
import java.util.List;

import me.akuz.ts.Seq;
import me.akuz.ts.TItem;

/**
 * Time series operations log, which arranges
 * all alerts by alert level to help identify
 * if the next operation should be stopped if
 * some errors have occurred before.
 *
 */
public final class TLog<T extends Comparable<T>> {
	
	private final Seq<T> _infosOrHigher;
	private final Seq<T> _warningsOrHigher;
	private final Seq<T> _dangerOrHigher;
	
	public TLog() {
		_infosOrHigher = new Seq<>();
		_warningsOrHigher = new Seq<>();
		_dangerOrHigher = new Seq<>();
	}
	/**
	 * Add an alert with provided level and message.
	 */
	public void add(final T time, final TLevel level, final String message) {
		add(time, new TAlert(level, message));
	}
	
	/**
	 * Add a pre-created alert.
	 */
	public void add(final T time, final TAlert alert) {
		
		if (alert.getLevel().compareTo(TLevel.Info) >= 0) {
			addTo(_infosOrHigher, time, alert);
		}
		if (alert.getLevel().compareTo(TLevel.Warning) >= 0) {
			addTo(_warningsOrHigher, time, alert);
		}
		if (alert.getLevel().compareTo(TLevel.Danger) >= 0) {
			addTo(_dangerOrHigher, time, alert);
		}
	}
	
	/**
	 * Collapses alerts at the same time into a list.
	 * 
	 */
	private static final <T extends Comparable<T>> void addTo(final Seq<T> seq, final T time, final TAlert alert) {
		
		final TItem<T> lastItem = seq.getLast();
		if (lastItem != null &&
			lastItem.getTime().equals(time)) {
			final List<TAlert> list = lastItem.get();
			list.add(alert);
		} else {
			final List<TAlert> list = new ArrayList<>(1);
			list.add(alert);
			seq.add(time, list);
		}
	}
	
	public boolean isEmpty() {
		return getCount() == 0;
	}
	
	public boolean hasAny() {
		return getCount() > 0;
	}
	
	public int getCount() {
		return _infosOrHigher.getItems().size();
	}
	
	public boolean hasInfosOrHigher() {
		return _infosOrHigher.getItems().size() > 0;
	}

	public int getInfosOrHigherCount() {
		return _infosOrHigher.getItems().size();
	}

	public Seq<T> getInfosOrHigher() {
		return _infosOrHigher;
	}
	
	public boolean hasWarningsOrHigher() {
		return _warningsOrHigher.getItems().size() > 0;
	}
	
	public int getWarningsOrHigherCount() {
		return _warningsOrHigher.getItems().size();
	}
	
	public Seq<T> getWarningsOrHigher() {
		return _warningsOrHigher;
	}
	
	public boolean hasDangerOrHigher() {
		return _dangerOrHigher.getItems().size() > 0;
	}
	
	public int getDangerOrHigherCount() {
		return _dangerOrHigher.getItems().size();
	}
	
	public Seq<T> getDangerOrHigher() {
		return _dangerOrHigher;
	}

}
