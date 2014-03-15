package me.akuz.qf.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.core.Pair;

import Jama.Matrix;


public final class CorrelateByTime {
	
	public static final PortfolioHistData buildPortfolioHistData(Map<String, List<DateVolumeClose>> map, Date minDate, Date maxDate) {
		
		List<Pair<Date, Map<String, DateVolumeClose>>> list = new ArrayList<>();
		
		Index<String> tickersIndex = new HashIndex<>();
		tickersIndex.ensureAll(map.keySet());
		
		TimeCorrelator timeCorrelator = new TimeCorrelator(map, minDate, maxDate);
		while (timeCorrelator.next()) {
			Date date = null;
			Map<String, DateVolumeClose> dateMap = new HashMap<>();
			for (String ticker : map.keySet()) {
				DateVolumeClose dvc = timeCorrelator.getCurrent(ticker);
				dateMap.put(ticker, dvc);
				if (dvc != null) {
					if (date == null) {
						date = dvc.getDate();
					} else if (!date.equals(dvc.getDate())) {
						throw new IllegalStateException("Uncorrelated dates");
					}
				}
			}
			if (date == null) {
				throw new IllegalStateException("No ccorrelated date");
			}
			list.add(new Pair<Date, Map<String,DateVolumeClose>>(date, dateMap));
		}

		List<Date> dates = new ArrayList<>();
		Matrix mPrices = new Matrix(list.size(), tickersIndex.size());
		Matrix mVolumes = new Matrix(list.size(), tickersIndex.size());
		
		for (int i=0; i<list.size(); i++) {
			
			Pair<Date, Map<String, DateVolumeClose>> pair = list.get(i);
			Date date = pair.v1();
			Map<String, DateVolumeClose> dateMap = pair.v2();
			
			dates.add(date);
			for (int tickerIndex=0; tickerIndex<tickersIndex.size(); tickerIndex++) {
				
				String ticker = tickersIndex.getValue(tickerIndex);
				DateVolumeClose dvc = dateMap.get(ticker);
				
				if (dvc != null) {
					mPrices.set(i, tickerIndex, dvc.getClose());
					mVolumes.set(i, tickerIndex, dvc.getVolume());
				} else {
					mPrices.set(i, tickerIndex, Double.NaN);
					mVolumes.set(i, tickerIndex, Double.NaN);
				}
			}
		}
		
		// create portfolio data
		PortfolioHistData data = new PortfolioHistData(tickersIndex, dates, mPrices, mVolumes);
		
		return data;
	}

}
