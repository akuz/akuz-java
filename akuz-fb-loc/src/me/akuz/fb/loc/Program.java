package me.akuz.fb.loc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.core.FileUtils;
import me.akuz.core.Pair;
import me.akuz.core.logs.LogUtils;

public class Program {

	public static void main(String[] args) throws IOException {
		
		LogUtils.configure(Level.INFO);
		Logger log = LogUtils.getLogger("main");
		
		final Place allPlaces = new Place(0L);
		final Map<Long, Place> places = new HashMap<>();

		// training
		{
			final String fileName = "/Users/andrey/Desktop/KaggleFB/train.csv";

			long lineIndex = 0;
			Scanner scanner = null;
			try {
				scanner = FileUtils.openScanner(fileName);
				while (scanner.hasNextLine()) {
					final String line = scanner.nextLine().trim();
					if (lineIndex > 0 && line.length() > 0) {
		
						String[] parts = null;
						try {
							parts = line.split(",");
						} catch (Exception ex) {
							log.severe(ex.toString());
							ex.printStackTrace(System.out);
							break;
						}
						
						if (parts.length != 6) {
							throw new IOException(
									"Wrong number of items (" + parts.length + 
									" in line index " + lineIndex);
						}
						
						final double x = Double.parseDouble(parts[1]);
						final double y = Double.parseDouble(parts[2]);
						final double accuracy = Double.parseDouble(parts[3]);
						final Long placeId = Long.parseLong(parts[5]);
						
						Place place = places.get(placeId);
						if (place == null) {
							place = new Place(placeId);
							places.put(placeId, place);
						}
						
						allPlaces.addObservation(x, y, accuracy);
						place.addObservation(x, y, accuracy);
						
						if (lineIndex % 100000 == 0) {
							log.info("Line  index: " + lineIndex);
							log.info("Place count: " + places.size());
						}
					}
					lineIndex++;
				}
			} catch (Exception ex) {
				log.severe(ex.toString());
				ex.printStackTrace(System.out);
			} finally {
				if (scanner != null) {
					scanner.close();
					scanner = null;
				}
			}
			log.info("Last  index: " + lineIndex);
			log.info("Place count: " + places.size());
		}
		
		// tree
		log.info("Making tree...");
		final Tree tree = new Tree(places.values(), places.size());

		// testing
		{
			final String testFileName = "/Users/andrey/Desktop/KaggleFB/test.csv";
			final String submitFileName = "/Users/andrey/Desktop/KaggleFB/submit.csv";
			String NL = System.getProperty("line.separator");

			long lineIndex = 0;
			Scanner scanner = null;
			Writer writer = null;
			try {
				scanner = FileUtils.openScanner(testFileName);
				FileOutputStream fos = new FileOutputStream(submitFileName);
				writer = new OutputStreamWriter(fos, FileUtils.UTF8);

				writer.write("row_id,place_id");
				writer.write(NL);

				while (scanner.hasNextLine()) {
					final String line = scanner.nextLine().trim();
					if (lineIndex > 0 && line.length() > 0) {
		
						String[] parts = null;
						try {
							parts = line.split(",");
						} catch (Exception ex) {
							log.severe(ex.toString());
							ex.printStackTrace(System.out);
							break;
						}
						
						if (parts.length != 5) {
							throw new IOException(
									"Wrong number of items (" + parts.length + 
									" in line index " + lineIndex);
						}
						
						final Long rowId = Long.parseLong(parts[0]);
						final double x = Double.parseDouble(parts[1]);
						final double y = Double.parseDouble(parts[2]);
						final double accuracy = Double.parseDouble(parts[3]);
						
						List<Pair<Place, Double>> top = tree.findTop(x, y, accuracy, 3);
						
						writer.write(rowId.toString());
						writer.write(",");
						for (int k=0; k<top.size(); k++) {
							if (k > 0) {
								writer.write(" ");
								System.out.print(" ");
							}
							final Place place = top.get(k).v1();
							writer.write(place.getId().toString());
							System.out.print(place.getId());
						}
						writer.write(NL);
						System.out.println();
						if (lineIndex % 100 == 0) {
							log.info("Test index: " + lineIndex);
						}
					}
					lineIndex++;
				}
			} catch (Exception ex) {
				log.severe(ex.toString());
				ex.printStackTrace(System.out);
			} finally {
				if (scanner != null) {
					scanner.close();
					scanner = null;
				}
				if (writer != null) {
					writer.flush();
					writer.close();
					writer = null;
				}
			}
		}
		
		log.info("DONE.");
	}

}
