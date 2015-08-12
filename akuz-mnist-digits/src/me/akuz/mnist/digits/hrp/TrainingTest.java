package me.akuz.mnist.digits.hrp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.akuz.core.geom.ByteImage;
import me.akuz.mnist.digits.load.MNIST;

public class TrainingTest {

	public static void main(String[] args) throws IOException {
		
		final String fileName = "/Users/andrey/SkyDrive/Documents/Data/digits_input/train.csv";
		final int maxImageCount = 500;
		
		final int[] dims = new int[] { 10, 10, 10, 10, 10 };
		final int iterationsPerLayer = 10;
		
		final MNIST mnist = MNIST.load(fileName, maxImageCount);
		
		final List<Image> images = new ArrayList<>(mnist.getImages().size());
		for (final ByteImage byteImage : mnist.getImages()) {
			images.add(new Image(byteImage));
		}
		
		final Training training = new Training(dims, images, iterationsPerLayer);
		training.execute();
		
		training.getModel().print();
	}

}
