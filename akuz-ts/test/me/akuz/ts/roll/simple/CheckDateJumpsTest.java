package me.akuz.ts.roll.simple;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.akuz.core.DateFmt;
import me.akuz.core.Period;
import me.akuz.ts.TFrame;
import me.akuz.ts.TSeq;
import me.akuz.ts.filters.TFrameStepper;
import me.akuz.ts.filters.TFrameFilter;
import me.akuz.ts.filters.simple.CheckDateGaps;
import me.akuz.ts.log.TLog;

import org.junit.Assert;
import org.junit.Test;

public class CheckDateJumpsTest {
	
	@Test
	public void testNoValues() throws ParseException {
		
		TFrame<String, Date> frame = new TFrame<>();
		frame.addSeq("f1", new TSeq<Date>());
		
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
		final CheckDateGaps checkDateJumps = new CheckDateGaps(infoAfterPeriod, warningAfterPeriod, errorAfterPeriod);
		
		final TFrameStepper<String, Date> frameAligner = new TFrameStepper<>(
				frame,
				frame.getKeys(),
				times);
		
		final TFrameFilter<String, Date> frameFilter = TFrameFilter
				.on(frameAligner)
				.addAllKeysFilter(checkDateJumps)
				.setLog(log)
				.build();
		
		int i=0;
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(0, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(0, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
		
		Assert.assertFalse(frameFilter.hasNext());
	}
	
	@Test
	public void testWithValues() throws ParseException {

		TFrame<String, Date> frame = new TFrame<>();
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
		
		final TFrameStepper<String, Date> frameAligner = new TFrameStepper<>(
				frame,
				frame.getKeys(),
				times);
		
		final TFrameFilter<String, Date> frameFilter = TFrameFilter
				.on(frameAligner)
				.addAllKeysFilter(checkDateJumps)
				.setLog(log)
				.build();
		
		int i=0;
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(0, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(0, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(0, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(1, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosCount());
		Assert.assertEquals(1, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosCount());
		Assert.assertEquals(2, log.getWarningsCount());
		Assert.assertEquals(1, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosCount());
		Assert.assertEquals(2, log.getWarningsCount());
		Assert.assertEquals(2, log.getErrorsCount());
		
		Assert.assertTrue(frameFilter.hasNext());
		frameFilter.next();
		Assert.assertEquals(frameFilter.getCurrTime(), times.get(i++));
		Assert.assertEquals(2, log.getInfosCount());
		Assert.assertEquals(2, log.getWarningsCount());
		Assert.assertEquals(2, log.getErrorsCount());
		
		Assert.assertFalse(frameFilter.hasNext());
	}

}
