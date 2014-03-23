package me.akuz.ts.derive;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Integerable;
import me.akuz.core.Stringable;

public final class TradingMode implements Stringable, Integerable {
	
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

	@Override
	public String convertToString() {
		return _name;
	}

	@Override
	public Object convertFromString(String str) {
		for (int i=0; i<All.size(); i++) {
			TradingMode mode = All.get(i);
			if (mode._name.equals(str)) {
				return mode;
			}
		}
		throw new IllegalArgumentException("Unknown " + getClass().getSimpleName() + ": " + str);
	}

	@Override
	public Integer convertToInteger() {
		return _id;
	}

	@Override
	public Object convertFromInteger(Integer num) {
		for (int i=0; i<All.size(); i++) {
			TradingMode mode = All.get(i);
			if (mode._id == num.intValue()) {
				return mode;
			}
		}
		throw new IllegalArgumentException("Unknown " + getClass().getSimpleName() + ": " + num);
	}
}
