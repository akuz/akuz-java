package me.akuz.qf.imprt;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import me.akuz.core.DateUtils;
import me.akuz.core.FileUtils;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;

public final class YahooDataRawLoad {

	public final static List<Pair<Double, Date>> loadAdjClose(String fileName, TimeZone timeZone, int closingHour) throws IOException, ParseException {
		
		List<Pair<Double, Date>> prices = new ArrayList<Pair<Double,Date>>();

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		fmt.setTimeZone(timeZone);

		List<String> lines = FileUtils.readEntireFileLines(fileName);
		for (int i=1; i<lines.size(); i++) {
			
			String line = lines.get(i).trim();
			if (line.length() > 0) {
				//2012-06-08,571.60,580.58,569.00,580.32,12395100,580.32
				String[] parts = line.split(",");
				if (parts.length != 7) {
					throw new IOException("Incorrect format in line #" + (i+1) + " of file " + fileName);
				}
				Date timeZoneDate = fmt.parse(parts[0]);
				Date priceDateTime = DateUtils.addHours(timeZoneDate, closingHour);
				Double price = Double.parseDouble(parts[6]);
				prices.add(new Pair<Double, Date>(price, priceDateTime));
			}
		}
		
		if (prices.size() > 1) {
			Collections.sort(prices, new PairComparator<Double, Date>(SortOrder.Asc));
		}

		return prices;
	}
}
