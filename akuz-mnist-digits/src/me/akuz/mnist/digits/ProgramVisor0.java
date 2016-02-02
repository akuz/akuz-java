package me.akuz.mnist.digits;

import java.io.IOException;

import me.akuz.ml.tensors.Tensor;
import me.akuz.mnist.digits.visor.learning.GaussColors;
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
		layer0.infer();
		
		// split channels
		final SplitLastDim layer1 = new SplitLastDim(layer0.output.shape, 1);
		layer1.setInput(layer0.output);
		layer1.infer();

		// finite colors (Y)
		final GaussColors layer2Y = new GaussColors(
				layer1.output1.shape, colorCountY);
		layer2Y.setInput(layer1.output1);

		// finite colors (C)
		final GaussColors layer2C = new GaussColors(
				layer1.output2.shape, colorCountC);
		layer2C.setInput(layer1.output2);
		
		// perform learning
		for (double temperature = 0.99; temperature > 0.1; temperature *= 0.5){
			
			System.out.println(
					colorCountY + " " + 
					colorCountC + " " + 
					temperature);
			
			layer2Y.infer();
			layer2Y.learn();
			
//			layer2C.setTemperature(temperature);
//			layer2C.setContrast(Math.pow(temperature, -1.0));
			layer2C.infer();
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
				".bmp",
				"bmp");
		
		System.out.println();
		System.out.println("DONE " + colorCountY + " x " + colorCountC + " colors.");
		System.out.println();
	}
	
	public static void main(String[] args) throws IOException {
		
//		final Tensor image = TensorGen.colourSineImage(150, 200);
		final Tensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/Inputs/baz.jpg");
//		final Tensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/Inputs/mount.png");
//		final Tensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/Inputs/andrey.jpg");
		
		TensorFiles.saveImage_sRGB(image, PREFIX + "0.bmp", "bmp");
		for (int colorCount=2; colorCount<=64; colorCount*=2) {
			approximate(
					image, 
					colorCount,
					colorCount);
//					Math.max(2, colorCount / 4));
		}
	}

}

