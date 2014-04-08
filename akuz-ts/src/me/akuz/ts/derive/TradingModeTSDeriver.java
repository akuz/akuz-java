package me.akuz.ts.derive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.ts.TS;
import me.akuz.ts.TSItem;
import me.akuz.ts.align.TSAlignIterator;

public final class TradingModeTSDeriver<T extends Comparable<T>> {
	
	private final static Integer TS_PRICE = 0;
	private final static Integer TS_ACTIVE_PERIOD = 1;
	
	public TradingModeTSDeriver() {
		// nothing
	}
	
	public TS<T> derive(List<T> times, TS<T> tsPrice, TS<T> tsActivePeriod) {
		
		final TS<T> tsTradingMode = new TS<>();
		
		final Map<Integer, TS<T>> iteratorMap = new HashMap<>();
		iteratorMap.put(TS_PRICE, tsPrice);
		iteratorMap.put(TS_ACTIVE_PERIOD, tsActivePeriod);
		
		TSAlignIterator<Integer, T> iterator = new TSAlignIterator<>(iteratorMap, times, iteratorMap.keySet());
		boolean rollingActivePeriod = false;
		while (iterator.hasNext()) {
			
			final Map<Integer, TSItem<T>> currValues = iterator.next();
			final T currTime = iterator.getCurrTime();
			final Double currPrice;
			{
				TSItem<T> currPriceItem = currValues.get(TS_PRICE);
				if (currPriceItem != null) {
					currPrice = currPriceItem.getDouble();
				} else {
					currPrice = null;
				}
			}
			final Boolean currActivePeriod;
			{
				TSItem<T> currActivePeriodItem = currValues.get(TS_ACTIVE_PERIOD);
				if (currActivePeriodItem != null) {
					currActivePeriod = currActivePeriodItem.getBoolean();
				} else {
					currActivePeriod = null;
				}
			}
			
			if (currActivePeriod != null) { // at active period border
			
				if (currActivePeriod) { // at active period start
					
					if (currPrice != null && !Double.isNaN(currPrice.doubleValue())) {
						tsTradingMode.add(new TSItem<>(currTime, TradingMode.Enabled));
					} else {
						tsTradingMode.add(new TSItem<>(currTime, TradingMode.KeepPos));
					}
					
				} else { // at active period end
					
					if (currPrice != null && !Double.isNaN(currPrice.doubleValue())) {
						tsTradingMode.add(new TSItem<T>(currTime, TradingMode.TradeOut));
					} else {
						throw new IllegalStateException("No price to trade out at the end of the active period");
					}
				}
				
				rollingActivePeriod = currActivePeriod.booleanValue();
				
			} else { // no changes in the active period
				
				if (rollingActivePeriod) {
					
					if (currPrice != null && !Double.isNaN(currPrice.doubleValue())) {
						tsTradingMode.add(new TSItem<T>(currTime, TradingMode.Enabled));
					} else {
						tsTradingMode.add(new TSItem<T>(currTime, TradingMode.KeepPos));
					}
					
				} else {
					
					tsTradingMode.add(new TSItem<T>(currTime, TradingMode.Disabled));
				}
			}
		}
		
		return tsTradingMode;
	}

}
