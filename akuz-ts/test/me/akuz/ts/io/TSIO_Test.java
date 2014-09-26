package me.akuz.ts.io;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.gson.GsonSerializers;
import me.akuz.ts.Frame;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class TSIO_Test {
	
	@Test
	public void differentDataTypesTest() throws IOException {
		
		Random rnd = ThreadLocalRandom.current();

		final IOMap<String> ioMap = new IOMap<>("t", IOType.IntegerType);
		ioMap.add("F boo", "F boo, field", IOType.BooleanType);
		ioMap.add("F dbl", IOType.DoubleType);
		ioMap.add("F int", "F int, field", IOType.IntegerType);
		ioMap.add("F str", IOType.StringType);

		final File file = new File("/tmp/" + UUID.randomUUID().toString() + ".csv");

		try {
			final String csv1;
			final String json1;
			{
				Frame<String, Integer> frame = new Frame<>();
				for (int i=0; i<27; i++) {
					frame.add("F boo", i, rnd.nextBoolean());
					frame.add("F dbl", i, rnd.nextDouble());
					frame.add("F int", i, rnd.nextInt());
					frame.add("F str", i, "" + rnd.nextDouble());
				}
				
				CSV_IO.toCSV(file, frame, ioMap);
				csv1 = CSV_IO.toCSV(frame, ioMap);
				json1 = GsonSerializers.getNoHtmlEscapingPretty().toJson(JSON_IO.toJson(frame, ioMap));
				System.out.println("1 ----------------------- CSV");
				System.out.println(csv1);
				System.out.println("1 ----------------------- JSON");
				System.out.println(json1);
			}
			
			final String csv2FromFile;
			final String csv2FromStr;
			final String json2;
			{
				final Frame<String, Integer> frameFromStr = CSV_IO.fromCSV(csv1, ioMap);
				csv2FromStr = CSV_IO.toCSV(frameFromStr, ioMap);
				
				final Frame<String, Integer> frameFromFile = CSV_IO.fromCSV(file, ioMap);
				csv2FromFile = CSV_IO.toCSV(frameFromFile, ioMap);
				
				JsonArray jsonArr2 = new JsonParser().parse(json1).getAsJsonArray();
				final Frame<String, Integer> frameJSON2 = JSON_IO.fromJson(jsonArr2, ioMap);
				json2 = GsonSerializers.getNoHtmlEscapingPretty().toJson(JSON_IO.toJson(frameJSON2, ioMap));
				
				System.out.println("2 ----------------------- CSV");
				System.out.println(csv2FromStr);
				System.out.println("2 ----------------------- JSON");
				System.out.println(json2);
			}
			Assert.assertEquals("CSV file serialization does not match", csv1, csv2FromFile);
			Assert.assertEquals("CSV string serialization does not match", csv1, csv2FromStr);
			Assert.assertEquals("JSON serialization does not match", json1, json2);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

}
