package me.akuz.mnist.digits;

import java.io.IOException;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.mnist.digits.visor.VisorLayerC;

public class ProgramVisor0 {
	
	private static final String PREFIX = "/Users/andrey/Desktop/test";
	
	private static void approximate(
			final DenseTensor image,
			final int colorCount,
			final int iterCount) throws IOException {

		final VisorLayerC layer = new VisorLayerC(image.shape, colorCount);
		
		layer.setInput(image);
		
		for (int i=0; i<iterCount; i++) {
			System.out.println(i);
			layer.infer(false);
			layer.learn();
		}
		
		System.out.println("dream");
		final DenseTensor dream = new DenseTensor(image.shape);
		layer.setInput(dream);
		layer.dream();
		
		TensorFiles.saveImage_sRGB(dream, PREFIX + colorCount + ".png");
		
		System.out.println();
		System.out.println("DONE " + colorCount + " colors.");
		System.out.println();
	}
	
	public static void main(String[] args) throws IOException {
		
//		final Tensor image = TensorGen.colourSineImage(150, 200);
		final DenseTensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/baz.jpg");
//		final Tensor image = TensorFiles.loadImage("/Users/andrey/Desktop/andrey.jpg");
		
		TensorFiles.saveImage_sRGB(image, PREFIX + "0.png");
		for (int colorCount=2; colorCount<=32; colorCount*=2) {
			approximate(image, colorCount, 50);
		}
	}

}

