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
	
	private final Seq<T> _all;
	private final Seq<T> _infos;
	private final Seq<T> _warnings;
	private final Seq<T> _errors;
	
	public TLog() {
		_all = new Seq<>();
		_infos = new Seq<>();
		_warnings = new Seq<>();
		_errors = new Seq<>();
	}
	/**
	 * Add an alert with provided level and message.
	 */
	public void add(final T time, final TLogLevel level, final String message) {
		add(time, new TLogAlert(level, message));
	}
	
	/**
	 * Add a pre-created alert.
	 */
	public void add(final T time, final TLogAlert alert) {
		
		if (alert.getLevel().equals(TLogLevel.Info)) {
			addTo(_infos, time, alert);
		} else if (alert.getLevel().equals(TLogLevel.Warning)) {
			addTo(_warnings, time, alert);
		} else if (alert.getLevel().equals(TLogLevel.Error)) {
			addTo(_errors, time, alert);
		} else {
			throw new IllegalArgumentException("Unsupported alert level: " + alert.getLevel());
		}
		addTo(_all, time, alert);
	}
	
	private static final <T extends Comparable<T>> void addTo(final Seq<T> seq, final T time, final TLogAlert alert) {
		
		final TItem<T> lastItem = seq.getLast();
		if (lastItem != null &&
			lastItem.getTime().equals(time)) {
			List<TLogAlert> list = lastItem.get();
			list.add(alert);
		} else {
			final List<TLogAlert> list = new ArrayList<>(1);
			list.add(alert);
			final TItem<T> newItem = new TItem<>(time, list);
			seq.add(newItem);
		}
	}
	
	public int size() {
		return _all.getItems().size();
	}
	
	public boolean hasAny() {
		return _all.getItems().size() > 0;
	}
	
	public Seq<T> getAll() {
		return _all;
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
