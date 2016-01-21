package me.akuz.mnist.digits;

import java.io.IOException;

import me.akuz.ml.tensors.Tensor;
import me.akuz.mnist.digits.visor.learning.FiniteColors2;
import me.akuz.mnist.digits.visor.transform.SplitLastDim;
import me.akuz.mnist.digits.visor.transform.YpCbCr;

public class ProgramVisor0 {
	
	private static final String PREFIX = "/Users/andrey/Desktop/test";
	
	private static void approximate(
			final Tensor image,
			final int colorCountY,
			final int colorCountC) throws IOException {
		
		// transform colors
		final YpCbCr layer0 = new YpCbCr(YpCbCr.Mode.NORMALIZE, image.shape);
		layer0.setInput(image);
		layer0.infer(false);
		
		// split channels
		final SplitLastDim layer1 = new SplitLastDim(layer0.output.shape, 1);
		layer1.setInput(layer0.output);
		layer1.infer(false);

		double temperature = 0.99;
		
		// finite colors (Y)
		final FiniteColors2 layer2Y = new FiniteColors2(
				layer1.output1.shape, colorCountY, temperature);
		layer2Y.setInput(layer1.output1);

		// finite colors (C)
		final FiniteColors2 layer2C = new FiniteColors2(
				layer1.output2.shape, colorCountC, temperature);
		layer2C.setInput(layer1.output2);
		
		// perform learning
		for (temperature = 0.99; temperature > 0.1; temperature /= 2){
			System.out.println(colorCountY + " " + colorCountC + " " + temperature);
			layer2Y.setTemperature(temperature);
			layer2Y.infer(false);
			layer2Y.learn();
			layer2C.setTemperature(temperature);
			layer2C.infer(false);
			layer2C.learn();
		}
		
		System.out.println("dream");
		final Tensor dream = new Tensor(image.shape);
		layer0.setInput(dream);
		layer2Y.dream();
		layer2C.dream();
		layer1.dream();
		layer0.dream();
		
		TensorFiles.saveImage_sRGB(dream, PREFIX + 
				"_" + colorCountY + 
				"_" + colorCountC + 
				".png");
		
		System.out.println();
		System.out.println("DONE " + colorCountY + " x " + colorCountC + " colors.");
		System.out.println();
	}
	
	public static void main(String[] args) throws IOException {
		
//		final Tensor image = TensorGen.colourSineImage(150, 200);
//		final Tensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/Inputs/baz.jpg");
		final Tensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/Inputs/mount.png");
//		final Tensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/Inputs/andrey.jpg");
		
		TensorFiles.saveImage_sRGB(image, PREFIX + "0.png");
		for (int colorCount=2; colorCount<=64; colorCount*=2) {
			approximate(
					image, 
					colorCount, 
					Math.max(2, colorCount / 4));
		}
	}

}

