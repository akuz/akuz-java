package me.akuz.ts.io.custom;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.DateFmt;
import me.akuz.core.DateUtils;
import me.akuz.core.FileUtils;
import me.akuz.core.Out;
import me.akuz.core.Pair;
import me.akuz.core.Rounding;
import me.akuz.ts.Frame;
import me.akuz.ts.Cube;
import me.akuz.ts.FrameIterator;
import me.akuz.ts.Quote;
import me.akuz.ts.QuoteField;
import me.akuz.ts.Seq;
import me.akuz.ts.TItem;

public final class YahooDataLoad {
	
	private static final Pattern _csvExtensionPattern = Pattern.compile("\\.csv$", Pattern.CASE_INSENSITIVE);
	
	public final static Seq<Date> loadDailyAdjQuoteSeq(
			final String fileName, 
			final Date minDate,
			final Date maxDate,
			final TimeZone timeZone,
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
		
		final Frame<QuoteField, Date> frame = loadDailyQuoteFieldFrame(
				fileName, 
				minDate, 
				maxDate, 
				timeZone, 
				EnumSet.of(
						QuoteField.AdjOpen,
						QuoteField.AdjClose,
						QuoteField.AdjVolume), 
				null);
		
		final Seq<Date> seq = new Seq<>();

		final FrameIterator<QuoteField, Date> iter = frame.iterator();
		final Out<Date> nextTime = new Out<>();
		while (iter.getNextTime(nextTime)) {
			
			iter.moveToTime(nextTime.getValue());
			
			final Date currTime = iter.getCurrTime();
			
			final TItem<Date> adjOpenItem = iter.getCurrItem(QuoteField.AdjOpen);
			final TItem<Date> adjCloseItem = iter.getCurrItem(QuoteField.AdjClose);
			final TItem<Date> adjVolumeItem = iter.getCurrItem(QuoteField.AdjVolume);
			
			if (adjOpenItem == null) {
				throw new IllegalStateException("AdjOpen item is null at " + currTime);
			}
			if (adjCloseItem == null) {
				throw new IllegalStateException("AdjClose item is null at " + currTime);
			}
			if (adjVolumeItem == null) {
				throw new IllegalStateException("AdjVolume item is null at " + currTime);
			}
			
			if (Double.isNaN(adjOpenItem.getDouble())) {
				throw new IllegalStateException("AdjOpen item is NaN at " + currTime);
			}
			if (Double.isNaN(adjCloseItem.getDouble())) {
				throw new IllegalStateException("AdjClose item is NaN at " + currTime);
			}
			if (Double.isNaN(adjVolumeItem.getDouble())) {
				throw new IllegalStateException("AdjVolume item is NaN at " + currTime);
			}
			
			final Date openTime = DateUtils.addHours(currTime, assumeOpenHour);
			final Date closeTime = DateUtils.addHours(currTime, assumeCloseHour);
			
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
	
	public static final Frame<String, Date> loadDailyTickerAdjQuoteFrame(
			final String dirPath, 
			final Date minDate,
			final Date maxDate,
			final Set<String> ignoreTickers, 
			final TimeZone timeZone,
			final int assumeOpenHour,
			final int assumeCloseHour) throws IOException, ParseException {
		
		final Frame<String, Date> frame = new Frame<>();
		final List<Pair<String, File>> tickerFiles = loadDirTickerFiles(dirPath, ignoreTickers);
		for (int i=0; i<tickerFiles.size(); i++) {
			
			final Pair<String, File> pair = tickerFiles.get(i);
			final String ticker = pair.v1();
			final File file = pair.v2();
			
			final Seq<Date> seq = loadDailyAdjQuoteSeq(
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
	
	public final static Frame<QuoteField, Date> loadDailyQuoteFieldFrame(
			final String fileName, 
			final Date minDate,
			final Date maxDate,
			final TimeZone timeZone, 
			final EnumSet<QuoteField> fields,
			final Set<Date> fillDateSet) throws IOException, ParseException {

		Frame<QuoteField, Date> frame = new Frame<>();
	
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
					if (fillDateSet != null) {
						fillDateSet.add(date);
					}
					
					final Double open      = Double.parseDouble(parts[1]);
					if (fields.contains(QuoteField.Open)) {
						frame.stage(QuoteField.Open, date, open);
					}
					final Double high      = Double.parseDouble(parts[2]);
					if (fields.contains(QuoteField.High)) {
						frame.stage(QuoteField.High, date, high);
					}
					final Double low       = Double.parseDouble(parts[3]);
					if (fields.contains(QuoteField.Low)) {
						frame.stage(QuoteField.Low, date, low);
					}
					final Double close     = Double.parseDouble(parts[4]);
					if (fields.contains(QuoteField.Close)) {
						frame.stage(QuoteField.Close, date, close);
					}
					final Double volume    = Double.parseDouble(parts[5]);
					if (fields.contains(QuoteField.Volume)) {
						frame.stage(QuoteField.Volume, date, volume);
					}
					final Double adjClose  = Double.parseDouble(parts[6]);
					if (fields.contains(QuoteField.AdjClose)) {
						frame.stage(QuoteField.AdjClose, date, adjClose);
					}
					final Double adjFactor = adjClose / close;
					final Double adjOpen   = Rounding.round(open * adjFactor, 4);
					if (fields.contains(QuoteField.AdjOpen)) {
						frame.stage(QuoteField.AdjOpen, date, adjOpen);
					}
					final Double adjHigh   = Rounding.round(high * adjFactor, 4);
					if (fields.contains(QuoteField.AdjHigh)) {
						frame.stage(QuoteField.AdjHigh, date, adjHigh);
					}
					final Double adjLow    = Rounding.round(low * adjFactor, 4);
					if (fields.contains(QuoteField.AdjLow)) {
						frame.stage(QuoteField.AdjLow, date, adjLow);
					}
					final Double adjVolume = Rounding.round(volume / adjFactor, 4);
					if (fields.contains(QuoteField.AdjVolume)) {
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
	
	public static final Cube<String, QuoteField, Date> loadDailyTickerQuoteFieldCube(
			final String dirPath, 
			final Date minDate,
			final Date maxDate,
			final Set<String> ignoreTickers, 
			final TimeZone timeZone,
			final EnumSet<QuoteField> fields,
			final Set<Date> fillDateSet) throws IOException, ParseException {
		
		final Cube<String, QuoteField, Date> cube = new Cube<>();
		final List<Pair<String, File>> tickerFiles = loadDirTickerFiles(dirPath, ignoreTickers);
		for (int i=0; i<tickerFiles.size(); i++) {
			
			final Pair<String, File> pair = tickerFiles.get(i);
			final String ticker = pair.v1();
			final File file = pair.v2();
			
			final Frame<QuoteField, Date> frame = loadDailyQuoteFieldFrame(
					file.getAbsolutePath(),
					minDate,
					maxDate,
					timeZone,
					fields,
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
