package me.akuz.ts.align;

import java.util.ArrayList;
import java.util.List;

public final class TSAlignLog {
	
	private List<TSAlignLogMsg> _infos;
	private List<TSAlignLogMsg> _warnings;
	private List<TSAlignLogMsg> _errors;
	
	public TSAlignLog() {
	}
	
	public void add(TSAlignLogMsg msg) {
		if (msg.getLevel().equals(TSAlignLogMsgLevel.Info)) {
			if (_infos == null) {
				_infos = new ArrayList<>();
			}
			_infos.add(msg);
		} else if (msg.getLevel().equals(TSAlignLogMsgLevel.Warning)) {
			if (_warnings == null) {
				_warnings = new ArrayList<>();
			}
			_warnings.add(msg);
		} else if (msg.getLevel().equals(TSAlignLogMsgLevel.Error)) {
			if (_errors == null) {
				_errors = new ArrayList<>();
			}
			_errors.add(msg);
		} else {
			throw new IllegalArgumentException("Unsupported log message level: " + msg.getLevel());
		}
	}
	public boolean hasInfos() {
		return _infos != null && _infos.size() > 0;
	}

	public List<TSAlignLogMsg> getInfos() {
		return _infos;
	}
	
	public boolean hasWarnings() {
		return _warnings != null && _warnings.size() > 0;
	}
	
	public List<TSAlignLogMsg> getWarnings() {
		return _warnings;
	}
	
	public boolean hasErrors() {
		return _errors != null && _errors.size() > 0;
	}
	
	public List<TSAlignLogMsg> getErrors() {
		return _errors;
	}

}
