package me.akuz.qf.data.imprt;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import me.akuz.core.DateUtils;
import me.akuz.core.FileUtils;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.qf.data.DateVolumeClose;

public final class OtherData {
	
	private static final Pattern _csvExtensionPattern = Pattern.compile("(\\.csv|\\.txt)$", Pattern.CASE_INSENSITIVE);
	
	public final static List<Pair<Double, Date>> loadClosePrices(String fileName, TimeZone timeZone, int closingHour) throws IOException, ParseException {
		
		List<Pair<Double, Date>> prices = new ArrayList<Pair<Double,Date>>();

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		fmt.setTimeZone(timeZone);

		List<String> lines = FileUtils.readEntireFileLines(fileName);
		for (int i=1; i<lines.size(); i++) {
			
			String line = lines.get(i).trim();
			if (line.length() > 0) {
				//2012-06-08,571.60,580.58,569.00,580.32,12395100
				String[] parts = line.split(",");
				if (parts.length != 6) {
					throw new IOException("Incorrect format in line #" + (i+1) + " of file " + fileName);
				}
				Date timeZoneDate = fmt.parse(parts[0]);
				Date priceDateTime = DateUtils.addHours(timeZoneDate, closingHour);
				Double price = Double.parseDouble(parts[4]);
				prices.add(new Pair<Double, Date>(price, priceDateTime));
			}
		}
		
		if (prices.size() > 1) {
			Collections.sort(prices, new PairComparator<Double, Date>(SortOrder.Asc));
		}

		return prices;
	}
	
	public final static List<DateVolumeClose> loadFileDateVolumeClose(String fileName, TimeZone timeZone, int closingHour) throws IOException, ParseException {
		
		List<DateVolumeClose> list = new ArrayList<>();

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		fmt.setTimeZone(timeZone);

		Scanner scanner = FileUtils.openScanner(fileName, "UTF-8");
		try {
			int lineNumber = 0;
			while (scanner.hasNextLine()) {

				lineNumber += 1;
				final String line = scanner.nextLine().trim();

				// first line is header
				if (lineNumber == 1) {
					continue;
				}
				
				if (line.length() > 0) {
					//2012-06-08,571.60,580.58,569.00,580.32,12395100
					String[] parts = line.split(",");
					if (parts.length != 6) {
						throw new IOException("Incorrect format in line #" + (lineNumber) + " of file " + fileName);
					}
					final Date timeZoneDate = fmt.parse(parts[0]);
					final Date priceDateTime = DateUtils.addHours(timeZoneDate, closingHour);
					final Double close = Double.parseDouble(parts[4]);
					final Integer volume = Integer.parseInt(parts[5]);
					list.add(new DateVolumeClose(priceDateTime, volume, close));
				}
			}
		} finally {
			scanner.close();
		}
		
		if (list.size() > 1) {
			Collections.sort(list);
		}

		return list;
	}
	
	public static final Map<String, List<DateVolumeClose>> loadDirDateVolumeClose(String dirPath, Set<String> ignoreTickers, TimeZone timeZone, int closingHour) throws IOException, ParseException {
		
		Map<String, List<DateVolumeClose>> map = new HashMap<>();
		List<File> files = FileUtils.getFiles(dirPath);
		for (int i=0; i<files.size(); i++) {
			File file = files.get(i);
			String ticker = _csvExtensionPattern.matcher(file.getName()).replaceAll("");
			if (ticker.startsWith(".")) {
				continue;
			}
			if (ignoreTickers != null && ignoreTickers.contains(ticker)) {
				continue;
			}
			if (map.containsKey(ticker)) {
				throw new IOException("Duplicate data file for ticker " + ticker + " in dir " + dirPath);
			}
			List<DateVolumeClose> list = loadFileDateVolumeClose(file.getAbsolutePath(), timeZone, closingHour);
			map.put(ticker, list);
		}
		return map;
	}

}
