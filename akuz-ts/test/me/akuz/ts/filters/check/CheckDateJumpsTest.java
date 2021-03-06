package me.akuz.ts.filters.check;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.akuz.core.DateFmt;
import me.akuz.core.TPeriod;
import me.akuz.ts.Frame;
import me.akuz.ts.FrameFilter;
import me.akuz.ts.Seq;
import me.akuz.ts.filters.check.CheckDateGaps;
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
		
		TPeriod infoAfterPeriod    = TPeriod.fromDays(1.5);
		TPeriod warningAfterPeriod = TPeriod.fromDays(2.0);
		TPeriod errorAfterPeriod   = TPeriod.fromDays(3.5);
		
		final TLog<Date> log = new TLog<>();
		final CheckDateGaps checkDateJumps = new CheckDateGaps(
				infoAfterPeriod,
				warningAfterPeriod,
				errorAfterPeriod);
		
		final FrameFilter<String, Date> frameFilter = new FrameFilter<>(frame.iterator());
		frameFilter.addFilter(frame.getKeys(), checkDateJumps);
		frameFilter.setLog(log);
		
		int i=0;
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(0, log.getInfosOrHigherCount());
		Assert.assertEquals(0, log.getWarningsOrHigherCount());
		Assert.assertEquals(0, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(0, log.getInfosOrHigherCount());
		Assert.assertEquals(0, log.getWarningsOrHigherCount());
		Assert.assertEquals(0, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosOrHigherCount());
		Assert.assertEquals(0, log.getWarningsOrHigherCount());
		Assert.assertEquals(0, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosOrHigherCount());
		Assert.assertEquals(1, log.getWarningsOrHigherCount());
		Assert.assertEquals(0, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(3, log.getInfosOrHigherCount());
		Assert.assertEquals(2, log.getWarningsOrHigherCount());
		Assert.assertEquals(1, log.getDangerOrHigherCount());
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
		
		TPeriod infoAfterPeriod    = TPeriod.fromDays(0.99);
		TPeriod warningAfterPeriod = TPeriod.fromDays(1.99);
		TPeriod errorAfterPeriod   = TPeriod.fromDays(2.99);
		
		final TLog<Date> log = new TLog<>();
		final CheckDateGaps checkDateJumps = new CheckDateGaps(infoAfterPeriod, warningAfterPeriod, errorAfterPeriod);
		
		final FrameFilter<String, Date> frameFilter = new FrameFilter<>(frame.iterator());
		frameFilter.addFilter(frame.getKeys(), checkDateJumps);
		frameFilter.setLog(log);
		
		int i=0;
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(0, log.getInfosOrHigherCount());
		Assert.assertEquals(0, log.getWarningsOrHigherCount());
		Assert.assertEquals(0, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosOrHigherCount());
		Assert.assertEquals(0, log.getWarningsOrHigherCount());
		Assert.assertEquals(0, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosOrHigherCount());
		Assert.assertEquals(1, log.getWarningsOrHigherCount());
		Assert.assertEquals(0, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(3, log.getInfosOrHigherCount());
		Assert.assertEquals(2, log.getWarningsOrHigherCount());
		Assert.assertEquals(1, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(3, log.getInfosOrHigherCount());
		Assert.assertEquals(2, log.getWarningsOrHigherCount());
		Assert.assertEquals(1, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(4, log.getInfosOrHigherCount());
		Assert.assertEquals(2, log.getWarningsOrHigherCount());
		Assert.assertEquals(1, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(5, log.getInfosOrHigherCount());
		Assert.assertEquals(3, log.getWarningsOrHigherCount());
		Assert.assertEquals(1, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(6, log.getInfosOrHigherCount());
		Assert.assertEquals(4, log.getWarningsOrHigherCount());
		Assert.assertEquals(2, log.getDangerOrHigherCount());
		
		frameFilter.moveToTime(times.get(i));
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(6, log.getInfosOrHigherCount());
		Assert.assertEquals(4, log.getWarningsOrHigherCount());
		Assert.assertEquals(2, log.getDangerOrHigherCount());
	}

}
