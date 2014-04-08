package me.akuz.ts.align;

public final class TSAlignLogMsg {
	
	private final TSAlignLogMsgLevel _level;
	private final String _text;
	
	public TSAlignLogMsg(TSAlignLogMsgLevel level, String text) {
		_level = level;
		_text = text;
	}
	
	public TSAlignLogMsgLevel getLevel() {
		return _level;
	}
	
	public String getText() {
		return _text;
	}
	
	@Override
	public String toString() {
		return _level + ": " + _text;
	}

}
