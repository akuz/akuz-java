package me.akuz.ts.io;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.ts.TFrame;
import me.akuz.ts.io.types.TSIOType;

import org.junit.Assert;
import org.junit.Test;

public class CSV_TSIO_Test {
	
	@Test
	public void differentDataTypesTest() throws IOException {
		
		Random rnd = ThreadLocalRandom.current();

		final TSIOMap<String> tsioMap = new TSIOMap<>();
		tsioMap.add("F boo", TSIOType.BooleanType);
		tsioMap.add("F dbl", TSIOType.DoubleType);
		tsioMap.add("F int", TSIOType.IntegerType);
		tsioMap.add("F str", TSIOType.StringType);
		
		final String csv1;
		{
			TFrame<String, Integer> frame = new TFrame<>();
			for (int i=0; i<27; i++) {
				frame.add("F boo", i, rnd.nextBoolean());
				frame.add("F dbl", i, rnd.nextDouble());
				frame.add("F int", i, rnd.nextInt());
				frame.add("F str", i, "" + rnd.nextDouble());
			}
			
			csv1 = CSV_TSIO.toCSV(frame, "t", TSIOType.IntegerType, tsioMap);
			System.out.println("1 -----------------------");
			System.out.println(csv1);
		}
		
		final String csv2;
		{
			final TFrame<String, Integer> frame = CSV_TSIO.fromCSV(csv1, "t", TSIOType.IntegerType, tsioMap);
			csv2 = CSV_TSIO.toCSV(frame, "t", TSIOType.IntegerType, tsioMap);
			System.out.println("2 -----------------------");
			System.out.println(csv2);
		}
		Assert.assertEquals("CSV serialization does not match", csv1, csv2);
	}

}
