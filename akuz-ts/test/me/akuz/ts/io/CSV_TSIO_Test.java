package me.akuz.ts.io;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.ts.TFrame;

import org.junit.Assert;
import org.junit.Test;

public class CSV_TSIO_Test {
	
	@Test
	public void differentDataTypesTest() throws IOException {
		
		Random rnd = ThreadLocalRandom.current();

		final IOMap<String> tsioMap = new IOMap<>("t", IOType.IntegerType);
		tsioMap.add("F boo", "F boo, field", IOType.BooleanType);
		tsioMap.add("F dbl", IOType.DoubleType);
		tsioMap.add("F int", "F int, field", IOType.IntegerType);
		tsioMap.add("F str", IOType.StringType);
		
		final String csv1;
		{
			TFrame<String, Integer> frame = new TFrame<>();
			for (int i=0; i<27; i++) {
				frame.add("F boo", i, rnd.nextBoolean());
				frame.add("F dbl", i, rnd.nextDouble());
				frame.add("F int", i, rnd.nextInt());
				frame.add("F str", i, "" + rnd.nextDouble());
			}
			
			csv1 = CSV_IO.toCSV(frame, tsioMap);
			System.out.println("1 -----------------------");
			System.out.println(csv1);
		}
		
		final String csv2;
		{
			final TFrame<String, Integer> frame = CSV_IO.fromCSV(csv1, tsioMap);
			csv2 = CSV_IO.toCSV(frame, tsioMap);
			System.out.println("2 -----------------------");
			System.out.println(csv2);
		}
		Assert.assertEquals("CSV serialization does not match", csv1, csv2);
	}

}
