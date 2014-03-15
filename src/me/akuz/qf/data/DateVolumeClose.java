package me.akuz.qf.data;

import java.util.Date;

public class DateVolumeClose extends SmallRecord implements Comparable<DateVolumeClose> {

	private static final Integer DATE = 0;
	private static final Integer VOLUME = 1;
	private static final Integer CLOSE = 2;
	private static final Integer SIZE = 3;
	
	public DateVolumeClose(Date date, Integer volume, Double close) {
		super(SIZE);
		super.set(DATE, date);
		super.set(VOLUME, volume);
		super.set(CLOSE, close);
	}
	
	public Date getDate() {
		return super.getDate(DATE);
	}
	
	public Integer getVolume() {
		return super.getInteger(VOLUME);
	}
	
	public Double getClose() {
		return super.getDouble(CLOSE);
	}

	@Override
	public int compareTo(DateVolumeClose o) {
		return getDate().compareTo(o.getDate());
	}
}
