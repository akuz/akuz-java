package me.akuz.ts.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Time series operations log, which arranges
 * all alerts by alert level to help identify
 * if the next operation should be stopped if
 * some errors have occurred before.
 *
 */
public final class TLog {
	
	private final List<TLogAlert> _all;
	private final List<TLogAlert> _infos;
	private final List<TLogAlert> _warnings;
	private final List<TLogAlert> _errors;
	
	public TLog() {
		_all = new ArrayList<>();
		_infos = new ArrayList<>();
		_warnings = new ArrayList<>();
		_errors = new ArrayList<>();
	}
	/**
	 * Add an alert with provided level and message.
	 */
	public void add(TLogLevel level, String message) {
		add(new TLogAlert(level, message));
	}
	
	/**
	 * Add a pre-created alert.
	 */
	public void add(TLogAlert alert) {
		if (alert.getLevel().equals(TLogLevel.Info)) {
			_infos.add(alert);
		} else if (alert.getLevel().equals(TLogLevel.Warning)) {
			_warnings.add(alert);
		} else if (alert.getLevel().equals(TLogLevel.Error)) {
			_errors.add(alert);
		} else {
			throw new IllegalArgumentException("Unsupported alert level: " + alert.getLevel());
		}
		_all.add(alert);
	}
	
	public int size() {
		return _all.size();
	}
	
	public boolean hasAny() {
		return _all.size() > 0;
	}
	
	public List<TLogAlert> getAll() {
		return _all;
	}
	
	public boolean hasInfos() {
		return _infos.size() > 0;
	}

	public int getInfosCount() {
		return _infos.size();
	}

	public List<TLogAlert> getInfos() {
		return _infos;
	}
	
	public boolean hasWarnings() {
		return _warnings.size() > 0;
	}
	
	public int getWarningsCount() {
		return _warnings.size();
	}
	
	public List<TLogAlert> getWarnings() {
		return _warnings;
	}
	
	public boolean hasErrors() {
		return _errors.size() > 0;
	}
	
	public int getErrorsCount() {
		return _errors.size();
	}
	
	public List<TLogAlert> getErrors() {
		return _errors;
	}

}
