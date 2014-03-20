package me.akuz.qf.run;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.json.JSONArray;

import me.akuz.core.FileUtils;
import me.akuz.core.JSONObj;
import me.akuz.core.StringUtils;
import me.akuz.core.SystemUtils;
import me.akuz.core.UtcDate;
import me.akuz.core.http.HttpGetCall;
import me.akuz.core.http.HttpGetKind;
import me.akuz.core.logs.Monitor;
import me.akuz.core.logs.SystemOutMonitor;
import me.akuz.qf.data.imprt.PortfolioDataSpec;

public final class DownloadPortfolioDataFromYahoo {
	
	private static final Pattern _newLinesPattern = Pattern.compile("\\n(\\r)?");
	private static final String ENCODING = "UTF-8";
	private static final String TIMEZONE = "UTC";
	
	public static void main(String[] args) throws ParseException, IOException {
		
		Monitor monitor = new SystemOutMonitor();
		
//		final String portfSpecFile = "/Users/andrey/SkyDrive/Documents/Data/yahoo/FTSE100.txt";
//		final String outputDirName = "/Users/andrey/SkyDrive/Documents/Data/yahoo/FTSE100_20140314;

//		final String portfSpecFile = "/Users/andrey/SkyDrive/Documents/Data/yahoo/NASDAQ100.txt";
//		final String outputDirName = "/Users/andrey/SkyDrive/Documents/Data/yahoo/NASDAQ100_20140314";

//		final String portfSpecFile = "/Users/andrey/SkyDrive/Documents/Data/yahoo/SP500_ENERGY.txt";
//		final String outputDirName = "/Users/andrey/SkyDrive/Documents/Data/yahoo/SP500_ENERGY_20140314";

//		final String portfSpecFile = "/Users/andrey/SkyDrive/Documents/Data/yahoo/SP500_IT.txt";
//		final String outputDirName = "/Users/andrey/SkyDrive/Documents/Data/yahoo/SP500_IT_20140314";

//		final String portfSpecFile = "/Users/andrey/SkyDrive/Documents/Data/yahoo/SP100.txt";
//		final String outputDirName = "/Users/andrey/SkyDrive/Documents/Data/yahoo/SP100_20140317";

//		final String portfSpecFile = "/Users/andrey/SkyDrive/Documents/Data/yahoo/CAC40.txt";
//		final String outputDirName = "/Users/andrey/SkyDrive/Documents/Data/yahoo/CAC40_20140319";

//		final String portfSpecFile = "/Users/andrey/SkyDrive/Documents/Data/yahoo/DAX30.txt";
//		final String outputDirName = "/Users/andrey/SkyDrive/Documents/Data/yahoo/DAX30_20140319";
		
		final String portfSpecFile = "/Users/andrey/SkyDrive/Documents/Data/yahoo/IBEX35.txt";
		final String outputDirName = "/Users/andrey/SkyDrive/Documents/Data/yahoo/IBEX35_20140319";
		
		FileUtils.isDirExistsOrCreate(outputDirName);
		
		final JSONObj spec;
		try {
			spec = JSONObj.fromFile(portfSpecFile, ENCODING);
		} catch (Exception ex) {
			monitor.write("Could not load portfolio data spec from: " + portfSpecFile, ex);
			return;
		}
		
		JSONArray tickers = null;
		if (spec.has(PortfolioDataSpec.TICKERS)) {
			tickers = spec.getJSONArray(PortfolioDataSpec.TICKERS);
		}
		if (tickers != null && tickers.length() > 0) {
			
			monitor.write(tickers.length() + " tickers in portfolio data spec.");
			
			UtcDate minDate = null;
			if (spec.has(PortfolioDataSpec.MINDATE)) {
				String minDateStr = spec.getString(PortfolioDataSpec.MINDATE);
				if (minDateStr != null) {
					minDate = new UtcDate(new Date(), UtcDate.NumbersDateOnlyFormatString, TIMEZONE);
					minDate.parse(minDateStr);
				}
			}
			UtcDate maxDate = null;
			if (spec.has(PortfolioDataSpec.MAXDATE)) {
				String maxDateStr = spec.getString(PortfolioDataSpec.MAXDATE);
				if (maxDateStr != null) {
					maxDate = new UtcDate(new Date(), UtcDate.NumbersDateOnlyFormatString, TIMEZONE);
					maxDate.parse(maxDateStr);
				}
			}

			monitor.write("Min date: " + (minDate == null ? "null" : minDate.toString()));
			monitor.write("Max date: " + (maxDate == null ? "null" : maxDate.toString()));

			// http://ichart.finance.yahoo.com/table.csv?s=GSK.L&d=2&e=15&f=2014&g=d&a=6&b=1&c=1988
			final String MIN_YEAR_CODE  = "c";
			final String MIN_MONTH_CODE = "a";
			final String MIN_DAY_CODE   = "b";
			final String MAX_YEAR_CODE  = "f";
			final String MAX_MONTH_CODE = "d";
			final String MAX_DAY_CODE   = "e";
			final String TICKER_CODE    = "s";
			
			for (int i=0; i<tickers.length(); i++) {
				
				final String ticker = (String)tickers.get(i);
				
				final StringBuilder sb = new StringBuilder();
				sb.append("http://ichart.finance.yahoo.com/table.csv");
				sb.append("?");
				sb.append(TICKER_CODE);
				sb.append("=");
				sb.append(ticker);
				
				if (minDate != null) {
					sb.append("&");
					sb.append(MIN_YEAR_CODE);
					sb.append("=");
					sb.append(minDate.getCal().get(Calendar.YEAR));
					
					sb.append("&");
					sb.append(MIN_MONTH_CODE);
					sb.append("=");
					sb.append(minDate.getCal().get(Calendar.MONTH) + 1);
					
					sb.append("&");
					sb.append(MIN_DAY_CODE);
					sb.append("=");
					sb.append(minDate.getCal().get(Calendar.DAY_OF_MONTH));
				}
				
				if (maxDate != null) {
					sb.append("&");
					sb.append(MAX_YEAR_CODE);
					sb.append("=");
					sb.append(maxDate.getCal().get(Calendar.YEAR));
					
					sb.append("&");
					sb.append(MAX_MONTH_CODE);
					sb.append("=");
					sb.append(maxDate.getCal().get(Calendar.MONTH) + 1);
					
					sb.append("&");
					sb.append(MAX_DAY_CODE);
					sb.append("=");
					sb.append(maxDate.getCal().get(Calendar.DAY_OF_MONTH));
				}
				final String url = sb.toString();
				
				monitor.write("Downloading " + ticker + "...");
				HttpGetCall httpGetCall = new HttpGetCall(2, HttpGetKind.Text, url, ENCODING);
				if (!httpGetCall.call()) {
					throw new IOException("Could not download data for " + ticker, httpGetCall.getException());
				}
				
				final String correctedLineBreaks = _newLinesPattern.matcher(httpGetCall.getResultText()).replaceAll(SystemUtils.lineSeparator());
				
				try {
					FileUtils.writeEntireFile(
							StringUtils.concatPath(outputDirName, ticker + ".csv"),
							correctedLineBreaks, 
							ENCODING);
				} catch (Exception ex) {
					throw new IOException("Could not write file for " + ticker, ex);
				}
			}
			
		} else {
			
			monitor.write("No tickers in the portfolio data spec.");
		}
		monitor.write("DONE.");
	}

}
