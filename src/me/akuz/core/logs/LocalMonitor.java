package me.akuz.core.logs;

public final class LocalMonitor implements Monitor {
	
	private final String _id;
	private final Monitor _parent;
	
	public LocalMonitor(String id, Monitor parent) {
		_id = id;
		_parent = parent;
	}
	
	@Override
	public void write(String message) {
		_parent.write(_id + ": " + message);
	}
	
	@Override
	public void write(String message, Throwable ex) {
		_parent.write(_id + ": " + message, ex);
	}

}
