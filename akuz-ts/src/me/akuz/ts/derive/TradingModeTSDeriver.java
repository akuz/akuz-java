package me.akuz.ts.derive;

import java.util.Map;

import me.akuz.core.Out;
import me.akuz.ts.Frame;
import me.akuz.ts.FrameIterator;
import me.akuz.ts.Seq;
import me.akuz.ts.TItem;

public final class TradingModeTSDeriver<T extends Comparable<T>> {
	
	private final static Integer SEQ_PRICE = 0;
	private final static Integer SEQ_ACTIVE_PERIOD = 1;
	
	public TradingModeTSDeriver() {
		// nothing
	}
	
	public Seq<T> derive(Seq<T> seqPrice, Seq<T> seqActivePeriod) {
		
		final Seq<T> seqTradingMode = new Seq<>();
		
		final Frame<Integer, T> iteratorFrame = new Frame<>();
		iteratorFrame.addSeq(SEQ_PRICE, seqPrice);
		iteratorFrame.addSeq(SEQ_ACTIVE_PERIOD, seqActivePeriod);
		
		FrameIterator<Integer, T> frameAligner = new FrameIterator<>(iteratorFrame);
		boolean rollingActivePeriod = false;
		final Out<T> nextTime = new Out<>();
		while (frameAligner.getNextTime(nextTime)) {
			
			frameAligner.moveToTime(nextTime.getValue());
			
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
