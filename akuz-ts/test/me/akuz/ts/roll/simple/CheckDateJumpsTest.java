package me.akuz.ts.roll.simple;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.akuz.core.DateFmt;
import me.akuz.core.Period;
import me.akuz.ts.Frame;
import me.akuz.ts.Seq;
import me.akuz.ts.filters.FrameFilter;
import me.akuz.ts.filters.simple.CheckDateGaps;
import me.akuz.ts.log.TLog;

import org.junit.Assert;
import org.junit.Test;

public class CheckDateJumpsTest {
	
	@Test
	public void testNoValues() throws ParseException {
		
		Frame<String, Date> frame = new Frame<>();
		frame.addSeq("f1", new Seq<Date>());
		
		List<Date> times = new ArrayList<>();
		times.add(DateFmt.parse("20130101", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130102", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130103", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130104", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130105", DateFmt.YYYYMMDD));
		
		Period infoAfterPeriod    = Period.fromDays(1.5);
		Period warningAfterPeriod = Period.fromDays(2.0);
		Period errorAfterPeriod   = Period.fromDays(3.5);
		
		final TLog log = new TLog();
		final CheckDateGaps checkDateJumps = new CheckDateGaps(
				infoAfterPeriod,
				warningAfterPeriod,
				errorAfterPeriod);
		
		final FrameFilter<String, Date> frameFilter = new FrameFilter<>(frame);
		frameFilter.addFilter(frame.getKeys(), checkDateJumps);
		frameFilter.setLog(log);
		
		int i=0;
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(0, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(0, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
	}
	
	@Test
	public void testWithValues() throws ParseException {

		Frame<String, Date> frame = new Frame<>();
		frame.add("f1", DateFmt.parse("20130101000000", DateFmt.YYYYMMDDHHMMSS), 0);
		frame.add("f1", DateFmt.parse("20130105000000", DateFmt.YYYYMMDDHHMMSS), 0);
		
		List<Date> times = new ArrayList<>();
		times.add(DateFmt.parse("20130101", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130102", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130103", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130104", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130105", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130106", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130107", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130108", DateFmt.YYYYMMDD));
		times.add(DateFmt.parse("20130109", DateFmt.YYYYMMDD));
		
		Period infoAfterPeriod    = Period.fromDays(0.99);
		Period warningAfterPeriod = Period.fromDays(1.99);
		Period errorAfterPeriod   = Period.fromDays(2.99);
		
		final TLog log = new TLog();
		final CheckDateGaps checkDateJumps = new CheckDateGaps(infoAfterPeriod, warningAfterPeriod, errorAfterPeriod);
		
		final FrameFilter<String, Date> frameFilter = new FrameFilter<>(frame);
		frameFilter.addFilter(frame.getKeys(), checkDateJumps);
		frameFilter.setLog(log);
		
		int i=0;
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(0, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosCount());
		Assert.assertEquals(2, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosCount());
		Assert.assertEquals(2, log.getWarningsCount());
		Assert.assertEquals(2, log.getErrorsCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosCount());
		Assert.assertEquals(2, log.getWarningsCount());
		Assert.assertEquals(2, log.getErrorsCount());
	}

}
