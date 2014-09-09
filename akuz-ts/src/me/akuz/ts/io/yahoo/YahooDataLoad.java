package me.akuz.ts.io.yahoo;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.TDate;
import me.akuz.core.TDateTime;
import me.akuz.core.FileUtils;
import me.akuz.core.Out;
import me.akuz.core.Pair;
import me.akuz.core.Rounding;
import me.akuz.ts.Cube;
import me.akuz.ts.Frame;
import me.akuz.ts.FrameIterator;
import me.akuz.ts.Quote;
import me.akuz.ts.QuoteField;
import me.akuz.ts.Seq;
import me.akuz.ts.TItem;

import org.joda.time.DateTimeZone;

/**
 * Functions to load Yahoo financial data.
 *
 */
public final class YahooDataLoad {
	
	private static final Pattern _csvExtensionPattern = Pattern.compile("\\.csv$", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Load "intraday" quotes for one stock.
	 * 
	 */
	public final static Seq<TDateTime> loadIntradayStockQuotes(
			final String fileName, 
			final TDate minDate,
			final TDate maxDate,
			final DateTimeZone timeZone,
			final int assumeOpenHour,
			final int assumeCloseHour) throws IOException, ParseException {
		
		if (assumeOpenHour <= 0 || assumeOpenHour >= 24) {
			throw new IllegalArgumentException("Open hour must be between 1 and 23");
		}
		if (assumeCloseHour <= 0 || assumeCloseHour >= 24) {
			throw new IllegalArgumentException("Close hour must be between 1 and 23");
		}
		if (assumeOpenHour >= assumeCloseHour) {
			throw new IllegalArgumentException("Open hour hour must < close hour");
		}
		
		final Frame<QuoteField, TDate> frame = loadDailyStockQuoteFields(
				fileName, 
				minDate, 
				maxDate, 
				EnumSet.of(
						QuoteField.AdjOpen,
						QuoteField.AdjClose,
						QuoteField.AdjVolume), 
				null);
		
		final Seq<TDateTime> seq = new Seq<>();

		final FrameIterator<QuoteField, TDate> iter = frame.iterator();
		final Out<TDate> nextTime = new Out<>();
		while (iter.getNextTime(nextTime)) {
			
			iter.moveToTime(nextTime.getValue());
			
			final TDate currDate = iter.getCurrTime();
			
			final TItem<TDate> adjOpenItem = iter.getCurrItem(QuoteField.AdjOpen);
			final TItem<TDate> adjCloseItem = iter.getCurrItem(QuoteField.AdjClose);
			final TItem<TDate> adjVolumeItem = iter.getCurrItem(QuoteField.AdjVolume);
			
			if (adjOpenItem == null) {
				throw new IllegalStateException("AdjOpen item is null at " + currDate);
			}
			if (adjCloseItem == null) {
				throw new IllegalStateException("AdjClose item is null at " + currDate);
			}
			if (adjVolumeItem == null) {
				throw new IllegalStateException("AdjVolume item is null at " + currDate);
			}
			
			if (Double.isNaN(adjOpenItem.getDouble())) {
				throw new IllegalStateException("AdjOpen item is NaN at " + currDate);
			}
			if (Double.isNaN(adjCloseItem.getDouble())) {
				throw new IllegalStateException("AdjClose item is NaN at " + currDate);
			}
			if (Double.isNaN(adjVolumeItem.getDouble())) {
				throw new IllegalStateException("AdjVolume item is NaN at " + currDate);
			}
			
			final TDateTime startOfDay = TDateTime.from(
					currDate.get().toDateTimeAtStartOfDay(timeZone));
			
			final TDateTime openTime = TDateTime.from(
					startOfDay.get().withHourOfDay(assumeOpenHour));
			
			final TDateTime closeTime = TDateTime.from(
					startOfDay.get().withHourOfDay(assumeCloseHour));
			
			seq.add(openTime, Quote.build()
					.set(QuoteField.AdjOpen, adjOpenItem.getDouble())
					.create());
			
			seq.add(closeTime, Quote.build()
					.set(QuoteField.AdjClose, adjCloseItem.getDouble())
					.set(QuoteField.AdjVolume, adjVolumeItem.getDouble())
					.create());
		}
		
		return seq;
	}
	
	/**
	 * Load "intraday" quotes for a portfolio.
	 * 
	 */
	public static final Frame<String, TDateTime> loadIntradayPortfolioQuotes(
			final String dirPath, 
			final TDate minDate,
			final TDate maxDate,
			final Set<String> ignoreTickers, 
			final DateTimeZone timeZone,
			final int assumeOpenHour,
			final int assumeCloseHour) throws IOException, ParseException {
		
		final Frame<String, TDateTime> frame = new Frame<>();
		final List<Pair<String, File>> tickerFiles = loadDirTickerFiles(dirPath, ignoreTickers);
		for (int i=0; i<tickerFiles.size(); i++) {
			
			final Pair<String, File> pair = tickerFiles.get(i);
			final String ticker = pair.v1();
			final File file = pair.v2();
			
			final Seq<TDateTime> seq = loadIntradayStockQuotes(
					file.getAbsolutePath(),
					minDate,
					maxDate,
					timeZone,
					assumeOpenHour,
					assumeCloseHour);
			
			frame.addSeq(ticker, seq);
		}
		return frame;
	}
	
	/**
	 * Load daily quotes for one stock.
	 * 
	 */
	public final static Seq<TDate> loadDailyStockQuotes(
			final String fileName, 
			final TDate minDate,
			final TDate maxDate,
			final EnumSet<QuoteField> quoteFields,
			final Set<TDate> fillDateSet) throws IOException, ParseException {

		Seq<TDate> seq = new Seq<>();
	
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
					final TDate date        = new TDate(parts[0]);
					if (date.compareTo(minDate) < 0) {
						continue;
					}
					if (date.compareTo(maxDate) > 0) {
						continue;
					}
					if (fillDateSet != null) {
						fillDateSet.add(date);
					}
					
					final Quote.Builder build = Quote.build();
					
					final Double open      = Double.parseDouble(parts[1]);
					if (quoteFields.contains(QuoteField.Open)) {
						build.set(QuoteField.Open, open);
					}
					final Double high      = Double.parseDouble(parts[2]);
					if (quoteFields.contains(QuoteField.High)) {
						build.set(QuoteField.High, high);
					}
					final Double low       = Double.parseDouble(parts[3]);
					if (quoteFields.contains(QuoteField.Low)) {
						build.set(QuoteField.Low, low);
					}
					final Double close     = Double.parseDouble(parts[4]);
					if (quoteFields.contains(QuoteField.Close)) {
						build.set(QuoteField.Close, close);
					}
					final Double volume    = Double.parseDouble(parts[5]);
					if (quoteFields.contains(QuoteField.Volume)) {
						build.set(QuoteField.Volume, volume);
					}
					final Double adjClose  = Double.parseDouble(parts[6]);
					if (quoteFields.contains(QuoteField.AdjClose)) {
						build.set(QuoteField.AdjClose, adjClose);
					}
					final Double adjFactor = adjClose / close;
					final Double adjOpen   = Rounding.round(open * adjFactor, 4);
					if (quoteFields.contains(QuoteField.AdjOpen)) {
						build.set(QuoteField.AdjOpen, adjOpen);
					}
					final Double adjHigh   = Rounding.round(high * adjFactor, 4);
					if (quoteFields.contains(QuoteField.AdjHigh)) {
						build.set(QuoteField.AdjHigh, adjHigh);
					}
					final Double adjLow    = Rounding.round(low * adjFactor, 4);
					if (quoteFields.contains(QuoteField.AdjLow)) {
						build.set(QuoteField.AdjLow, adjLow);
					}
					final Double adjVolume = Rounding.round(volume / adjFactor, 4);
					if (quoteFields.contains(QuoteField.AdjVolume)) {
						build.set(QuoteField.AdjVolume, adjVolume);
					}
					
					seq.stage(date, build.create());
				}
			}
		} finally {
			scanner.close();
		}

		seq.acceptStaged();
		return seq;
	}

	/**
	 * Load daily quotes for a portfolio.
	 * 
	 */
	public static final Frame<String, TDate> loadDailyPortfolioQuotes(
			final String dirPath, 
			final TDate minDate,
			final TDate maxDate,
			final Set<String> ignoreTickers, 
			final EnumSet<QuoteField> quoteFields,
			final Set<TDate> fillDateSet) throws IOException, ParseException {
		
		final Frame<String, TDate> frame = new Frame<>();
		final List<Pair<String, File>> tickerFiles = loadDirTickerFiles(dirPath, ignoreTickers);
		for (int i=0; i<tickerFiles.size(); i++) {
			
			final Pair<String, File> pair = tickerFiles.get(i);
			final String ticker = pair.v1();
			final File file = pair.v2();
			
			final Seq<TDate> seq = loadDailyStockQuotes(
					file.getAbsolutePath(),
					minDate,
					maxDate,
					quoteFields,
					fillDateSet);
			
			frame.addSeq(ticker, seq);
		}
		return frame;
	}
	
	/**
	 * Load daily quote fields for one stock.
	 * 
	 */
	public final static Frame<QuoteField, TDate> loadDailyStockQuoteFields(
			final String fileName, 
			final TDate minDate,
			final TDate maxDate,
			final EnumSet<QuoteField> quoteFields,
			final Set<TDate> fillDateSet) throws IOException, ParseException {

		Frame<QuoteField, TDate> frame = new Frame<>();
	
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
					final TDate date        = new TDate(parts[0]);
					if (date.compareTo(minDate) < 0) {
						continue;
					}
					if (date.compareTo(maxDate) > 0) {
						continue;
					}
					if (fillDateSet != null) {
						fillDateSet.add(date);
					}
					
					final Double open      = Double.parseDouble(parts[1]);
					if (quoteFields.contains(QuoteField.Open)) {
						frame.stage(QuoteField.Open, date, open);
					}
					final Double high      = Double.parseDouble(parts[2]);
					if (quoteFields.contains(QuoteField.High)) {
						frame.stage(QuoteField.High, date, high);
					}
					final Double low       = Double.parseDouble(parts[3]);
					if (quoteFields.contains(QuoteField.Low)) {
						frame.stage(QuoteField.Low, date, low);
					}
					final Double close     = Double.parseDouble(parts[4]);
					if (quoteFields.contains(QuoteField.Close)) {
						frame.stage(QuoteField.Close, date, close);
					}
					final Double volume    = Double.parseDouble(parts[5]);
					if (quoteFields.contains(QuoteField.Volume)) {
						frame.stage(QuoteField.Volume, date, volume);
					}
					final Double adjClose  = Double.parseDouble(parts[6]);
					if (quoteFields.contains(QuoteField.AdjClose)) {
						frame.stage(QuoteField.AdjClose, date, adjClose);
					}
					final Double adjFactor = adjClose / close;
					final Double adjOpen   = Rounding.round(open * adjFactor, 4);
					if (quoteFields.contains(QuoteField.AdjOpen)) {
						frame.stage(QuoteField.AdjOpen, date, adjOpen);
					}
					final Double adjHigh   = Rounding.round(high * adjFactor, 4);
					if (quoteFields.contains(QuoteField.AdjHigh)) {
						frame.stage(QuoteField.AdjHigh, date, adjHigh);
					}
					final Double adjLow    = Rounding.round(low * adjFactor, 4);
					if (quoteFields.contains(QuoteField.AdjLow)) {
						frame.stage(QuoteField.AdjLow, date, adjLow);
					}
					final Double adjVolume = Rounding.round(volume / adjFactor, 4);
					if (quoteFields.contains(QuoteField.AdjVolume)) {
						frame.stage(QuoteField.AdjVolume, date, adjVolume);
					}
				}
			}
		} finally {
			scanner.close();
		}

		frame.acceptStaged();
		return frame;
	}
	
	/**
	 * Load daily quote fields for a portfolio.
	 * 
	 */
	public static final Cube<String, QuoteField, TDate> loadDailyPortfolioQuoteFields(
			final String dirPath, 
			final TDate minDate,
			final TDate maxDate,
			final Set<String> ignoreTickers, 
			final EnumSet<QuoteField> quoteFields,
			final Set<TDate> fillDateSet) throws IOException, ParseException {
		
		final Cube<String, QuoteField, TDate> cube = new Cube<>();
		final List<Pair<String, File>> tickerFiles = loadDirTickerFiles(dirPath, ignoreTickers);
		for (int i=0; i<tickerFiles.size(); i++) {
			
			final Pair<String, File> pair = tickerFiles.get(i);
			final String ticker = pair.v1();
			final File file = pair.v2();
			
			final Frame<QuoteField, TDate> frame = loadDailyStockQuoteFields(
					file.getAbsolutePath(),
					minDate,
					maxDate,
					quoteFields,
					fillDateSet);
			
			cube.addFrame(ticker, frame);
		}
		return cube;
	}
	
	/**
	 * Helper function to load ticker files from a dir.
	 * 
	 */
	public static final List<Pair<String, File>> loadDirTickerFiles(
			final String dirPath, 
			final Set<String> ignoreTickers) throws IOException, ParseException {
		
		final List<Pair<String, File>> list = new ArrayList<>();
		final List<File> files = FileUtils.getFiles(dirPath);
		for (int i=0; i<files.size(); i++) {
			final File file = files.get(i);
			if (file.isFile()) {
				final Matcher csvMatcher = _csvExtensionPattern.matcher(file.getName());
				if (csvMatcher.find()) {
					final String ticker = csvMatcher.reset().replaceAll("");
					if (ignoreTickers != null && ignoreTickers.contains(ticker)) {
						continue;
					}
					list.add(new Pair<>(ticker, file));
				}
			}
		}
		return list;
	}
}
