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
	
	private final Seq<T> _infos;
	private final Seq<T> _warnings;
	private final Seq<T> _errors;
	
	public TLog() {
		_infos = new Seq<>();
		_warnings = new Seq<>();
		_errors = new Seq<>();
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
			addTo(_infos, time, alert);
		}
		if (alert.getLevel().compareTo(TLevel.Warning) >= 0) {
			addTo(_warnings, time, alert);
		}
		if (alert.getLevel().compareTo(TLevel.Error) >= 0) {
			addTo(_errors, time, alert);
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
	
	public int size() {
		return _infos.getItems().size();
	}
	
	public boolean hasAny() {
		return size() > 0;
	}
	
	public boolean hasInfos() {
		return _infos.getItems().size() > 0;
	}

	public int getInfosCount() {
		return _infos.getItems().size();
	}

	public Seq<T> getInfos() {
		return _infos;
	}
	
	public boolean hasWarnings() {
		return _warnings.getItems().size() > 0;
	}
	
	public int getWarningsCount() {
		return _warnings.getItems().size();
	}
	
	public Seq<T> getWarnings() {
		return _warnings;
	}
	
	public boolean hasErrors() {
		return _errors.getItems().size() > 0;
	}
	
	public int getErrorsCount() {
		return _errors.getItems().size();
	}
	
	public Seq<T> getErrors() {
		return _errors;
	}

}
