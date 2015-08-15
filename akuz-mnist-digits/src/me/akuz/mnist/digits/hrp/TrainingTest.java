package me.akuz.mnist.digits.hrp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.akuz.mnist.digits.load.MNIST;
import me.akuz.mnist.digits.load.MNISTImage;

public class TrainingTest {

	public static void main(String[] args) throws IOException {
		
		final String fileName = "/Users/andrey/SkyDrive/Documents/Data/digits_input/train.csv";
		final int maxImageCount = 500;
		
		final List<MNISTImage> mnistImages = MNIST.load(fileName, maxImageCount);
		
		final List<LayerConfig> layerConfigs = new ArrayList<>();
		layerConfigs.add(new LayerConfig(80, Spread.SPATIAL));
		layerConfigs.add(new LayerConfig(40, Spread.SPATIAL));
		layerConfigs.add(new LayerConfig(10, Spread.SPATIAL));
		layerConfigs.add(new LayerConfig( 4, Spread.ALTERNATE));

		final Training training = new Training(mnistImages, layerConfigs);
		training.execute(10, 10);
	}

}
