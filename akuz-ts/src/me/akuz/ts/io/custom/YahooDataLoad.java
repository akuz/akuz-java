package me.akuz.ts.io.custom;

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

import me.akuz.core.DateAK;
import me.akuz.core.DateTimeAK;
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

public final class YahooDataLoad {
	
	private static final Pattern _csvExtensionPattern = Pattern.compile("\\.csv$", Pattern.CASE_INSENSITIVE);
	
	public final static Seq<DateTimeAK> loadDailyAdjQuoteSeq(
			final String fileName, 
			final DateAK minDate,
			final DateAK maxDate,
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
		
		final Frame<QuoteField, DateAK> frame = loadDailyQuoteFieldFrame(
				fileName, 
				minDate, 
				maxDate, 
				EnumSet.of(
						QuoteField.AdjOpen,
						QuoteField.AdjClose,
						QuoteField.AdjVolume), 
				null);
		
		final Seq<DateTimeAK> seq = new Seq<>();

		final FrameIterator<QuoteField, DateAK> iter = frame.iterator();
		final Out<DateAK> nextTime = new Out<>();
		while (iter.getNextTime(nextTime)) {
			
			iter.moveToTime(nextTime.getValue());
			
			final DateAK currDate = iter.getCurrTime();
			
			final TItem<DateAK> adjOpenItem = iter.getCurrItem(QuoteField.AdjOpen);
			final TItem<DateAK> adjCloseItem = iter.getCurrItem(QuoteField.AdjClose);
			final TItem<DateAK> adjVolumeItem = iter.getCurrItem(QuoteField.AdjVolume);
			
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
			
			final DateTimeAK startOfDay = DateTimeAK.from(
					currDate.get().toDateTimeAtStartOfDay(timeZone));
			
			final DateTimeAK openTime = DateTimeAK.from(
					startOfDay.get().withHourOfDay(assumeOpenHour));
			
			final DateTimeAK closeTime = DateTimeAK.from(
					startOfDay.get().withHourOfDay(assumeCloseHour));
			
			final Quote quote = Quote.build()
					.set(QuoteField.AdjOpen, adjOpenItem.getDouble())
					.set(QuoteField.AdjClose, adjCloseItem.getDouble())
					.set(QuoteField.AdjVolume, adjVolumeItem.getDouble())
					.set(QuoteField.OpenTime, openTime)
					.create();
			
			seq.add(closeTime, quote);
		}
		
		return seq;
	}
	
	public static final Frame<String, DateTimeAK> loadDailyTickerAdjQuoteFrame(
			final String dirPath, 
			final DateAK minDate,
			final DateAK maxDate,
			final Set<String> ignoreTickers, 
			final DateTimeZone timeZone,
			final int assumeOpenHour,
			final int assumeCloseHour) throws IOException, ParseException {
		
		final Frame<String, DateTimeAK> frame = new Frame<>();
		final List<Pair<String, File>> tickerFiles = loadDirTickerFiles(dirPath, ignoreTickers);
		for (int i=0; i<tickerFiles.size(); i++) {
			
			final Pair<String, File> pair = tickerFiles.get(i);
			final String ticker = pair.v1();
			final File file = pair.v2();
			
			final Seq<DateTimeAK> seq = loadDailyAdjQuoteSeq(
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
	
	public final static Frame<QuoteField, DateAK> loadDailyQuoteFieldFrame(
			final String fileName, 
			final DateAK minDate,
			final DateAK maxDate,
			final EnumSet<QuoteField> quoteFields,
			final Set<DateAK> fillDateSet) throws IOException, ParseException {

		Frame<QuoteField, DateAK> frame = new Frame<>();
	
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
					final DateAK date        = new DateAK(parts[0]);
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
	
	public static final Cube<String, QuoteField, DateAK> loadDailyTickerQuoteFieldCube(
			final String dirPath, 
			final DateAK minDate,
			final DateAK maxDate,
			final Set<String> ignoreTickers, 
			final EnumSet<QuoteField> quoteFields,
			final Set<DateAK> fillDateSet) throws IOException, ParseException {
		
		final Cube<String, QuoteField, DateAK> cube = new Cube<>();
		final List<Pair<String, File>> tickerFiles = loadDirTickerFiles(dirPath, ignoreTickers);
		for (int i=0; i<tickerFiles.size(); i++) {
			
			final Pair<String, File> pair = tickerFiles.get(i);
			final String ticker = pair.v1();
			final File file = pair.v2();
			
			final Frame<QuoteField, DateAK> frame = loadDailyQuoteFieldFrame(
					file.getAbsolutePath(),
					minDate,
					maxDate,
					quoteFields,
					fillDateSet);
			
			cube.addFrame(ticker, frame);
		}
		return cube;
	}
	
	public static final List<Pair<String, File>> loadDirTickerFiles(
			final String dirPath, 
			final Set<String> ignoreTickers) throws IOException, ParseException {
		
		final List<Pair<String, File>> list = new ArrayList<>();
		final List<File> files = FileUtils.getFiles(dirPath);
		for (int i=0; i<files.size(); i++) {
			final File file = files.get(i);
			final Matcher csvMatcher = _csvExtensionPattern.matcher(file.getName());
			if (csvMatcher.find()) {
				String ticker = csvMatcher.reset().replaceAll("");
				if (ignoreTickers != null && ignoreTickers.contains(ticker)) {
					continue;
				}
				list.add(new Pair<>(ticker, file));
			}
		}
		return list;
	}
}
