package me.akuz.ts.derive;

import java.util.ArrayList;
import java.util.List;

public final class TradingMode {
	
	public static final TradingMode Disabled  = new TradingMode(0, "Disabled");
	public static final TradingMode Enabled   = new TradingMode(1, "Enabled");
	public static final TradingMode KeepPos   = new TradingMode(2, "KeepPos");
	public static final TradingMode TradeOut  = new TradingMode(3, "TradeOut");
	public static final List<TradingMode> All;
	
	static {
		All = new ArrayList<>();
		All.add(Disabled);
		All.add(Enabled);
		All.add(KeepPos);
		All.add(TradeOut);
	}
	
	private final int _id;
	private final String _name;
	
	public static final TradingMode fromId(Integer id) {
		if (id == null) {
			throw new IllegalArgumentException("Cannot convert to " + TradingMode.class.getSimpleName() + " from null");
		}
		for (int i=0; i<All.size(); i++) {
			TradingMode mode = All.get(i);
			if (id.intValue() == mode._id) {
				return mode;
			}
		}
		throw new IllegalArgumentException("Cannot find " + TradingMode.class.getSimpleName() + " for id " + id);
	}
	
	public static final TradingMode fromName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Cannot convert to " + TradingMode.class.getSimpleName() + " from null");
		}
		name = name.trim();
		for (int i=0; i<All.size(); i++) {
			TradingMode mode = All.get(i);
			if (name.equalsIgnoreCase(mode._name)) {
				return mode;
			}
		}
		throw new IllegalArgumentException("Cannot find " + TradingMode.class.getSimpleName() + " for name '" + name + "'");
	}
	
	/**
	 * Private constructor to limit the instances to static properties only.
	 * 
	 */
	private TradingMode(int id, String name) {
		_id = id;
		_name = name;
	}
	
	public int getId() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	@Override
	public String toString() {
		return _name;
	}
}
