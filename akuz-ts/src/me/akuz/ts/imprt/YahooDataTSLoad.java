package me.akuz.ts.imprt;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.DateFmt;
import me.akuz.core.FileUtils;
import me.akuz.ts.TSMap;
import me.akuz.ts.TSMapMap;
import me.akuz.ts.TSSortMap;

public final class YahooDataTSLoad {
	
	private static final Pattern _csvExtensionPattern = Pattern.compile("\\.csv$", Pattern.CASE_INSENSITIVE);
	
	public final static TSMap<QuoteField, Date> loadFileTSMap(
			final String fileName, 
			final Date minDate,
			final Date maxDate,
			final TimeZone timeZone, 
			final EnumSet<QuoteField> fields) throws IOException, ParseException {

		TSSortMap<QuoteField, Date> tsSortMap = new TSSortMap<>();
	
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
					//2012-06-08,571.60,580.58,569.00,580.32,12395100,580.32
					String[] parts = line.split(",");
					if (parts.length != 7) {
						throw new IOException("Incorrect format in line #" + (lineNumber) + " of file " + fileName);
					}
					final Date date        = DateFmt.parse(parts[0], DateFmt.YYYYMMDD_dashed, timeZone);
					if (date.compareTo(minDate) < 0) {
						continue;
					}
					if (date.compareTo(maxDate) > 0) {
						continue;
					}
					final Double open      = Double.parseDouble(parts[1]);
					if (fields.contains(QuoteField.Open)) {
						tsSortMap.add(QuoteField.Open, date, open);
					}
					final Double high      = Double.parseDouble(parts[2]);
					if (fields.contains(QuoteField.High)) {
						tsSortMap.add(QuoteField.High, date, high);
					}
					final Double low       = Double.parseDouble(parts[3]);
					if (fields.contains(QuoteField.Low)) {
						tsSortMap.add(QuoteField.Low, date, low);
					}
					final Double close     = Double.parseDouble(parts[4]);
					if (fields.contains(QuoteField.Close)) {
						tsSortMap.add(QuoteField.Close, date, close);
					}
					final Double volume    = Double.parseDouble(parts[5]);
					if (fields.contains(QuoteField.Volume)) {
						tsSortMap.add(QuoteField.Volume, date, volume);
					}
					final Double adjClose  = Double.parseDouble(parts[6]);
					if (fields.contains(QuoteField.AdjClose)) {
						tsSortMap.add(QuoteField.AdjClose, date, adjClose);
					}
					final Double adjFactor = adjClose / close;
					final Double adjOpen   = open * adjFactor;
					if (fields.contains(QuoteField.AdjOpen)) {
						tsSortMap.add(QuoteField.AdjOpen, date, adjOpen);
					}
					final Double adjHigh   = high * adjFactor;
					if (fields.contains(QuoteField.AdjHigh)) {
						tsSortMap.add(QuoteField.AdjHigh, date, adjHigh);
					}
					final Double adjLow    = low * adjFactor;
					if (fields.contains(QuoteField.AdjLow)) {
						tsSortMap.add(QuoteField.AdjLow, date, adjLow);
					}
					final Double adjVolume = volume / adjFactor;
					if (fields.contains(QuoteField.AdjVolume)) {
						tsSortMap.add(QuoteField.AdjVolume, date, adjVolume);
					}
				}
			}
		} finally {
			scanner.close();
		}

		TSMap<QuoteField, Date> tsMap = tsSortMap.build();
		return tsMap;
	}
	
	public static final TSMapMap<String, QuoteField, Date> loadDirTSMapMap(
			final String dirPath, 
			final Date minDate,
			final Date maxDate,
			final Set<String> ignoreTickers, 
			final TimeZone timeZone,
			final EnumSet<QuoteField> fields) throws IOException, ParseException {
		
		TSMapMap<String, QuoteField, Date> mapMap = new TSMapMap<>();
		List<File> files = FileUtils.getFiles(dirPath);
		for (int i=0; i<files.size(); i++) {
			File file = files.get(i);
			Matcher csvMatcher = _csvExtensionPattern.matcher(file.getName());
			if (csvMatcher.find()) {
				String ticker = csvMatcher.reset().replaceAll("");
				if (ignoreTickers != null && ignoreTickers.contains(ticker)) {
					continue;
				}
				TSMap<QuoteField, Date> map = loadFileTSMap(
						file.getAbsolutePath(),
						minDate,
						maxDate,
						timeZone,
						fields);
				mapMap.add(ticker, map);
			}
		}
		return mapMap;
	}
}
