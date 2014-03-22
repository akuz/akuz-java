//package me.akuz.ts.analysis;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import me.akuz.ts.TS;
//import me.akuz.ts.TSEntry;
//
//public final class TSIsTradingPeriod extends TS<Date> {
//	
//	private final List<TSEntry<Date>> _sorted;
//	
//	public TSIsTradingPeriod(final TS<Date> tsPrice, final long turnOnPeriodMs, final long turnOffPriodMs) {
//		
//		_sorted = new ArrayList<>();
//		
//		boolean isOn = false;
//		Date lastDate = null;
//		
//		List<TSEntry<Date>> priceSorted = tsPrice.getSorted();
//		for (int i=0; i<priceSorted.size(); i++) {
//			
//			TSEntry<Date> tsEntry = priceSorted.get(i);
//			Date date = tsEntry.getTime();
//			Double price = tsEntry.getDouble();
//			
//			
//			
//			lastDate = date;
//		}
//	}
//	
//	@Override
//	public List<TSEntry<Date>> getSorted() {
//		return _sorted;
//	}
//
//}
