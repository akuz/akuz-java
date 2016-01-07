package me.akuz.mnist.digits.test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.StringUtils;
import me.akuz.core.geom.ByteImage;
import me.akuz.mnist.digits.Feature;
import me.akuz.mnist.digits.InferBlocks;

import org.junit.Test;

public class InferBlocksSpeedTest {
	
	@Test
	public void testSpeed() {
		
		Random rnd = ThreadLocalRandom.current();
		
		final int IMAGE_SIZE = 28;
		final int BLOCK_COUNT = 10;
		final int FEATURE_SIZE = 9;
		final int FEATURE_COUNT = 10;
		
		Feature[] features = new Feature[FEATURE_COUNT];
		for (int f=0; f<features.length; f++) {
			final double mean = f < features.length/2 ? 0.25 : 0.75;
			features[f] = new Feature(FEATURE_SIZE, mean, 1, Math.pow(0.1, 2), 1);
		}
		
		byte[][] data = new byte[IMAGE_SIZE][IMAGE_SIZE];
		for (int i=0; i<data.length; i++) {
			rnd.nextBytes(data[i]);
			if (i<data.length/2) {
				for (int j=0; j<data[i].length; j++) {
					data[i][j] = (byte)(+Math.abs(data[i][j]));
				}
			} else {
				for (int j=0; j<data[i].length; j++) {
					data[i][j] = (byte)(-Math.abs(data[i][j]));
				}
			}
		}
		
		ByteImage image = new ByteImage(data);
		InferBlocks inferBlocks = new InferBlocks(IMAGE_SIZE, BLOCK_COUNT, features);
		
		System.out.println("Calculating time...");
		long ms = System.currentTimeMillis();
		
		for (int s=1; s<=10; s++) {
			
			inferBlocks.infer(image);
			
			if (s % 10 == 0) {
				System.out.println("Average time (" + s + "): " + (System.currentTimeMillis() - ms) / (double) s);
			}
		}
		
		System.out.println("Block probs: " + StringUtils.arrayToString(inferBlocks.getBlockProbs(), ", "));
		System.out.println("          X: " + StringUtils.arrayToString(inferBlocks.getBlockX(), ", "));
		System.out.println("          Y: " + StringUtils.arrayToString(inferBlocks.getBlockY(), ", "));
		
		for (int b=0; b<BLOCK_COUNT; b++) {
			System.out.println("Block #" + (b+1) + " feature probs: " + StringUtils.arrayToString(inferBlocks.getBlockFeatureProbs()[b], ", "));
		}
		
	}

}
