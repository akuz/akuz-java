package me.akuz.ts.derive;

import java.util.Collection;
import java.util.Map;

import me.akuz.ts.TFrame;
import me.akuz.ts.Seq;
import me.akuz.ts.TItem;
import me.akuz.ts.filters.FrameWalker;

public final class TradingModeTSDeriver<T extends Comparable<T>> {
	
	private final static Integer SEQ_PRICE = 0;
	private final static Integer SEQ_ACTIVE_PERIOD = 1;
	
	public TradingModeTSDeriver() {
		// nothing
	}
	
	public Seq<T> derive(Collection<T> times, Seq<T> seqPrice, Seq<T> seqActivePeriod) {
		
		final Seq<T> seqTradingMode = new Seq<>();
		
		final TFrame<Integer, T> iteratorFrame = new TFrame<>();
		iteratorFrame.addSeq(SEQ_PRICE, seqPrice);
		iteratorFrame.addSeq(SEQ_ACTIVE_PERIOD, seqActivePeriod);
		
		FrameWalker<Integer, T> frameAligner = new FrameWalker<>(iteratorFrame, iteratorFrame.getKeys(), times);
		boolean rollingActivePeriod = false;
		while (frameAligner.hasNext()) {
			
			frameAligner.next();
			
			final Map<Integer, TItem<T>> currValues = frameAligner.getCurrItems();
			final T currTime = frameAligner.getCurrTime();
			final Double currPrice;
			
			{
				TItem<T> currPriceItem = currValues.get(SEQ_PRICE);
				if (currPriceItem != null) {
					currPrice = currPriceItem.getDouble();
				} else {
					currPrice = null;
				}
			}
			final Boolean currActivePeriod;
			{
				TItem<T> currActivePeriodItem = currValues.get(SEQ_ACTIVE_PERIOD);
				if (currActivePeriodItem != null) {
					currActivePeriod = currActivePeriodItem.getBoolean();
				} else {
					currActivePeriod = null;
				}
			}
			
			if (currActivePeriod != null) { // at active period border
			
				if (currActivePeriod) { // at active period start
					
					if (currPrice != null && !Double.isNaN(currPrice.doubleValue())) {
						seqTradingMode.add(new TItem<>(currTime, TradingMode.Enabled));
					} else {
						seqTradingMode.add(new TItem<>(currTime, TradingMode.KeepPos));
					}
					
				} else { // at active period end
					
					if (currPrice != null && !Double.isNaN(currPrice.doubleValue())) {
						seqTradingMode.add(new TItem<T>(currTime, TradingMode.TradeOut));
					} else {
						throw new IllegalStateException("No price to trade out at the end of the active period");
					}
				}
				
				rollingActivePeriod = currActivePeriod.booleanValue();
				
			} else { // no changes in the active period
				
				if (rollingActivePeriod) {
					
					if (currPrice != null && !Double.isNaN(currPrice.doubleValue())) {
						seqTradingMode.add(new TItem<T>(currTime, TradingMode.Enabled));
					} else {
						seqTradingMode.add(new TItem<T>(currTime, TradingMode.KeepPos));
					}
					
				} else {
					
					seqTradingMode.add(new TItem<T>(currTime, TradingMode.Disabled));
				}
			}
		}
		
		return seqTradingMode;
	}

}
