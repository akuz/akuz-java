package me.akuz.qf.data;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import me.akuz.core.Index;
import me.akuz.core.Out;
import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;
import Jama.Matrix;

public final class PortfolioHistData {
	
	private final Index<String> _tickersIndex;
	private final List<Date> _dates;
	private final Matrix _mPrices;
	private final Matrix _mVolumes;
	
	public PortfolioHistData(Index<String> tickersIndex, List<Date> dates, Matrix mPrices, Matrix mVolumes) {
		_tickersIndex = tickersIndex;
		_dates = dates;
		_mPrices = mPrices;
		_mVolumes = mVolumes;
	}
	
	public Index<String> getTickersIndex() {
		return _tickersIndex;
	}
	
	public List<Date> getDates() {
		return _dates;
	}
	
	public Matrix getPrices() {
		return _mPrices;
	}
	
	public Matrix getVolumes() {
		return _mVolumes;
	}
	
	private static final boolean isBoundedPositive(double value) {
		return isBoundedPositive(value, null);
	}
	private static final boolean isBoundedPositive(double value, Out<String> out) {
		if (Double.isNaN(value)) {
			if (out != null) {
				out.setValue("NAN");
			}
			return false;
		}
		if (Double.isInfinite(value)) {
			if (out != null) {
				out.setValue("INF");
			}
			return false;
		}
		if (Math.abs(value) < 0.00000001) {
			if (out != null) {
				out.setValue("ZER");
			}
			return false;
		}
		if (value < 0) {
			if (out != null) {
				out.setValue("NEG");
			}
			return false;
		}
		return true;
	}
	
	public static final boolean isBoundedNonNegative(double value) {
		return isBoundedNonNegative(value, null);
	}
	private static final boolean isBoundedNonNegative(double value, Out<String> out) {
		if (Double.isNaN(value)) {
			if (out != null) {
				out.setValue("NAN");
			}
			return false;
		}
		if (Double.isInfinite(value)) {
			if (out != null) {
				out.setValue("INF");
			}
			return false;
		}
		if (value < 0) {
			if (out != null) {
				out.setValue("NEG");
			}
			return false;
		}
		return true;
	}
	
	private static final double ratio(double one, double two) {
		return Math.min(one, two) / Math.max(one, two);
	}
	
	public boolean validate() {
		
		final DecimalFormat fmt = new DecimalFormat("0.##########");
		final double PRICE_RATIO_ERROR_THRESHOLD = 0.5;
		final double PRICE_RATIO_WARNING_THRESHOLD = 0.7;
		final double VOLUME_RATIO_WARNING_THRESHOLD = 0.05;
		
		boolean hasError = false;
		
		for (int j=0; j<_tickersIndex.size(); j++) {

			for (int i=0; i<_dates.size(); i++) {
				{
					double currPrice = _mPrices.get(i, j);
					Out<String> currReason = new Out<String>(null);
					if (!isBoundedPositive(currPrice, currReason)) {
						if (i > 0) {
							double prevPrice = _mPrices.get(i-1, j);
							Out<String> prevReason = new Out<>(null);
							if (isBoundedPositive(prevPrice, prevReason)) {
								warning(i, j, "Price is " + currReason + ", backfilling with " + fmt.format(prevPrice));
								_mPrices.set(i, j, prevPrice);
							} else {
								error(i, j, "Price is " + currReason + ", not backfilling (prev price is " + prevReason + ")");
								hasError = true;
							}
						} else {
							error(i, j, "Price is " + currReason + ", not backfilling (no previous price)");
							hasError = true;
						}
					} else if (i > 0) {
						
						double prevPrice = _mPrices.get(i-1, j);
						if (isBoundedPositive(prevPrice)) {
							if (ratio(currPrice, prevPrice) < PRICE_RATIO_ERROR_THRESHOLD) {
								error(i, j, "PRICE JUMP: " + fmt.format(prevPrice) + " -> " + fmt.format(currPrice));
								hasError = true;
							} else if (ratio(currPrice, prevPrice) < PRICE_RATIO_WARNING_THRESHOLD) {
								warning(i, j, "PRICE JUMP: " + fmt.format(prevPrice) + " -> " + fmt.format(currPrice));
							}
						}
					}
				}
				{
					double currVolume = _mVolumes.get(i, j);
					Out<String> currReason = new Out<String>(null);
					if (!isBoundedNonNegative(currVolume, currReason)) {
						warning(i, j, "Volume is " + currReason + ", backfilling with 0");
						_mVolumes.set(i, j, 0);
					} else if (i > 0) {
						double prevVolume = _mVolumes.get(i-1, j);
						if (isBoundedPositive(prevVolume)) {
							if (ratio(currVolume, prevVolume) < VOLUME_RATIO_WARNING_THRESHOLD) {
								warning(i, j, "Volume jump: " + fmt.format(prevVolume) + " -> " + fmt.format(currVolume));
							}
						}
					}
				}
			}
		}
		return !hasError;
	}
	
	private void error(int i, int j, String str) {
		log("#ERROR#", i, j, str);
	}
	
	private void warning(int i, int j, String str) {
		log("Warning", i, j, str);
	}
	
	private void log(String type, int i, int j, String str) {
		
		Date date = _dates.get(i);
		String ticker = _tickersIndex.getValue(j);
		System.out.println(
				type + " - " 
				+ new UtcDate(date, UtcDate.NumbersDateOnlyFormatString).toString()
				+ " - " 
				+ StringUtils.trimOrFillSpaces(ticker, 10)
				+ " - " 
				+ str);
	}

}
